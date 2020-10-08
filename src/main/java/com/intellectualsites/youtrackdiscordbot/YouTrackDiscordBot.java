package com.intellectualsites.youtrackdiscordbot;

import cloud.commandframework.Description;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.jda.JDACommandManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.intellectualsites.youtrackdiscordbot.modules.BotModule;
import com.intellectualsites.youtrackdiscordbot.modules.ConfigurationModule;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application entrypoint
 */
public final class YouTrackDiscordBot {

    private static final Logger logger = LoggerFactory.getLogger(YouTrackDiscordBot.class);

    private final Injector injector;
    private final JDA jda;
    private final JDACommandManager<MessageReceivedEvent> manager;

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
            throw  new IllegalStateException("Could not initialize command manager");
        }
        /* Create ping command */
        this.manager.command(
                this.manager.commandBuilder("ping", Description.of("Ping the bot"))
                            .withPermission("admin")
                            .argument(StringArgument.optional("msg", StringArgument.StringMode.GREEDY))
                            .handler(context -> {
                                final String message = context.getOrDefault("msg", "pong");
                                context.getSender().getTextChannel().sendMessage(message).submit();
                            })
        );
    }

    public static void main(final @NonNull String @NonNull [] args) {
        new YouTrackDiscordBot();
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

}
