package com.intellectualsites.youtrackdiscordbot;

import cloud.commandframework.Description;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.intellectualsites.youtrackdiscordbot.config.BotConfiguration;
import com.intellectualsites.youtrackdiscordbot.modules.BotModule;
import com.intellectualsites.youtrackdiscordbot.modules.ConfigurationModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.llorllale.youtrack.api.AssignedField;
import org.llorllale.youtrack.api.Issue;
import org.llorllale.youtrack.api.Project;
import org.llorllale.youtrack.api.YouTrack;
import org.llorllale.youtrack.api.session.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Application entrypoint
 */
public final class YouTrackDiscordBot {

    private static final Logger logger = LoggerFactory.getLogger(YouTrackDiscordBot.class);
    private static final Predicate<String> issuePattern = Pattern.compile("([A-Za-z]+)-([0-9]+)").asPredicate();

    private final Injector injector;
    private final JDA jda;
    private final JDACommandManager<MessageReceivedEvent> manager;
    private final YouTrack youTrackAPI;
    private final List<Project> validProjects = new ArrayList<>();

    private final Map<String, Integer> latestIssues = new HashMap<>();

    private YouTrackDiscordBot() {
        try {
            this.injector = Guice.createInjector(
                    Stage.DEVELOPMENT,
                    new ConfigurationModule(),
                    new BotModule(this)
            );
        } catch (final Exception e) {
            logger.error("Failed to create injector", e);
            throw new IllegalStateException("Could not initialize application");
        }
        try {
            this.jda = this.injector.getInstance(BotFactory.class).createBot();
        } catch (final Exception e) {
            logger.error("Failed to create JDA instance", e);
            throw new IllegalStateException("Could not initialize application");
        }
        try {
            this.manager = this.injector.getInstance(CommandManagerFactory.class).create(jda);
        } catch (final Exception e) {
            logger.error("Failed to create command manager", e);
            throw new IllegalStateException("Could not initialize command manager");
        }
        try {
            this.youTrackAPI = this.injector.getInstance(YouTrackFactory.class).create();
        } catch (final Exception e) {
            logger.error("Failed to create YouTrack API", e);
            throw new IllegalStateException("Could not initialize command manager");
        }
        final BotConfiguration configuration = this.injector.getInstance(BotConfiguration.class);
        /* Create ping command */
        this.manager.command(
                this.manager.commandBuilder("ping", Description.of("Ping the bot"))
                        .withPermission("admin")
                        .argument(StringArgument.optional("msg", StringArgument.StringMode.GREEDY))
                        .handler(context -> {
                            final String message = context.getOrDefault("msg", "pong");
                            context.getSender().getTextChannel().sendMessage(message).submit();
                        })
        ).command(
                this.manager.commandBuilder("reload", Description.of("Reload projects & configurations"))
                        .withPermission("admin")
                        .handler(context -> {
                            configuration.loadConfiguration();
                            context.getSender().getTextChannel().sendMessage("... Reloaded configuration").submit();
                            this.validProjects.clear();
                            try {
                                this.reloadProjects(context);
                            } catch (final Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException("Something went wrong...", e);
                            }
                            context.getSender().getTextChannel().sendMessage("Reload done!").submit();
                        })
        ).command((this.manager.commandBuilder("show", Description.of("Show an issue")))
                .flag(this.manager.flagBuilder("desc").build())
                .argument(
                        StringArgument.of("id"),
                        Description.of("Issue ID")
                ).handler(context -> {
                    final String id = context.get("id");
                    if (!issuePattern.test(id)) {
                        context.getSender().getTextChannel().sendMessage(
                                String.format(
                                        "Usage: `%s show project-id` | `%s` is of the wrong format",
                                        configuration.getPrefix(),
                                        id
                                )
                        ).submit();
                        return;
                    }

                    final String[] parts = id.split(Pattern.quote("-"));

                    Project workingProject = null;
                    for (final Project project : this.validProjects) {
                        if (project.name().equalsIgnoreCase(parts[0])
                                || project.id().equalsIgnoreCase(parts[0])) {
                            workingProject = project;
                            break;
                        }
                    }

                    if (workingProject == null) {
                        context.getSender().getTextChannel().sendMessage(
                                String.format(
                                        "Unrecognized project `%s`",
                                        parts[0]
                                )
                        ).submit();
                        return;
                    }

                    try {
                        Integer.parseInt(parts[1]);
                    } catch (final Exception e) {
                        context.getSender().getTextChannel().sendMessage(
                                String.format(
                                        "'%s' is not a valid issue number",
                                        parts[1]
                                )
                        ).submit();
                        return;
                    }

                    Issue issue;
                    try {
                        final Optional<Issue> issueOptional = workingProject.issues().get(
                                String.format(
                                        "%s-%s",
                                        workingProject.id(),
                                        parts[1]
                                )
                        );
                        if (!issueOptional.isPresent()) {
                            context.getSender().getTextChannel().sendMessage(
                                    String.format(
                                            "There is no issue with ID '%s'",
                                            parts[1]
                                    )
                            ).submit();
                            return;
                        }
                        issue = issueOptional.get();
                    } catch (final Exception e) {
                        context.getSender().getTextChannel().sendMessage(
                                String.format(
                                        "Failed to retrieve issue: %s",
                                        e.getMessage()
                                )
                        ).submit();
                        e.printStackTrace();
                        return;
                    }

                    try {
                        final EmbedBuilder embed = this.createEmbed(issue);

                        if (context.flags().isPresent("desc")) {
                            issue.description().ifPresent(description -> {
                                if (description.length() > 500) {
                                    description = description.substring(0, 500) + "...";
                                }
                                embed.addField("Description", description, false);
                            });
                        }

                        context.getSender().getChannel().sendMessage(embed.build()).submit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
        ).command(this.manager.commandBuilder("refresh", Description.of("Fetch latest issues"))
                .withPermission("admin")
                .handler(context -> {
                    context.getSender().getTextChannel().sendMessage("Fetching new issues...").submit();
                    for (final Issue issue : this.refreshIssues()) {
                        context.getSender().getTextChannel().sendMessage(this.createEmbed(issue).build()).submit();
                    }
                }));
        this.reloadProjects(null);

        new Timer("YouTrack Broadcaster").scheduleAtFixedRate(
                new BroadcastTimerTask(configuration),
                TimeUnit.MINUTES.toMillis(1L),
                TimeUnit.MINUTES.toMillis(1L)
        );
    }

    public static void main(final @NonNull String @NonNull [] args) {
        new YouTrackDiscordBot();
    }

    private void reloadProjects(final @Nullable CommandContext<MessageReceivedEvent> context) {
        this.injector.getInstance(BotConfiguration.class).getYouTrackProjects().stream().map(project -> {
            try {
                final Optional<Project> ytProject = this.youTrackAPI.projects().get(project);
                if (!ytProject.isPresent()) {
                    if (context != null) {
                        context.getSender().getTextChannel().sendMessage(
                                String.format(
                                        "... Could not read project '%s'",
                                        project
                                )
                        ).submit();
                    }
                } else {
                    return ytProject.get();
                }
            } catch (final UnauthorizedException e) {
                logger.error("The client is not allowed to access {}", project);
            } catch (final IOException e) {
                logger.error("Something went wrong when reading projects", e);
            }
            return null;
        }).filter(Objects::nonNull).forEach(project -> {
            this.validProjects.add(project);
            if (context != null) {
                context.getSender().getTextChannel().sendMessage(
                        String.format(
                                "... Found project: %s (%s)",
                                project.name(),
                                project.id()
                        )
                ).submit();
            }
        });
        this.latestIssues.putAll(this.getLatestIssues());
    }

    private @NonNull Map<@NonNull String, @NonNull Integer> getLatestIssues() {
        final Map<String, Integer> issues = new HashMap<>();
        this.validProjects.forEach(project -> {
            try {
                final List<String> issueNames = project.issues().stream().map(Issue::id).collect(Collectors.toList());
                final int id = Integer.parseInt(
                        issueNames.get(issueNames.size() - 1).substring(project.id().length() + 1)
                );
                logger.info("Last issue project {} is {}", project.id(), id);
                issues.put(project.id(), id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return issues;
    }

    private @NonNull EmbedBuilder createEmbed(final @NonNull Issue issue) {
        final EmbedBuilder embed = new EmbedBuilder()
                .setTitle(issue.summary(), String.format(
                        "%s/issue/%s",
                        this.injector.getInstance(BotConfiguration.class).getYouTrackURL(),
                        issue.id()
                ))
                .addField("Project", issue.project().name(), true)
                .addField("ID", issue.id(), true)
                .setTimestamp(issue.creationDate());
        for (final AssignedField assignedField : issue.fields()) {
            embed.addField(assignedField.name(), assignedField.value().asString(), true);
        }
        return embed;
    }

    private @NonNull Collection<@NonNull Issue> refreshIssues() {
        final List<Issue> newIssues = new LinkedList<>();
        for (final Project project : this.validProjects) {
            final int latestIssue = this.latestIssues.get(project.id());
            try {
                for (final Issue issue : project.issues().stream().collect(Collectors.toList())) {
                    final int id = Integer.parseInt(
                            issue.id().substring(project.id().length() + 1)
                    );
                    if (id > latestIssue) {
                        newIssues.add(
                                project.issues().get(issue.id()).orElseThrow(
                                        () -> new IllegalStateException("Failed to fetch issue meta data")
                                )
                        );
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        this.latestIssues.putAll(this.getLatestIssues());
        return newIssues;
    }

    /**
     * Get the JDA instance powering the bot
     *
     * @return JDA instance
     */
    public @NonNull JDA getJDA() {
        return this.jda;
    }

    /**
     * Get the command manager instance
     *
     * @return Command manager
     */
    public @NonNull JDACommandManager<MessageReceivedEvent> getCommandManager() {
        return this.manager;
    }

    /**
     * Get the YouTrack API instance
     *
     * @return API instance
     */
    public @NonNull YouTrack getYouTrackAPI() {
        return this.youTrackAPI;
    }

    private class BroadcastTimerTask extends TimerTask {

        private final BotConfiguration configuration;

        private BroadcastTimerTask(final @NonNull BotConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void run() {
            logger.info("Running broadcast task...");
            for (final Issue issue : refreshIssues()) {
                final TextChannel textChannel = (TextChannel) jda.getGuildChannelById(
                        this.configuration.getBroadcastChannels().get(issue.project().id())
                );
                textChannel.sendMessage(createEmbed(issue).build()).submit();
            }
        }

    }

}
