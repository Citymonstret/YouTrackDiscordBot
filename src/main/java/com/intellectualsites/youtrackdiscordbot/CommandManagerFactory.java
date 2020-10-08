package com.intellectualsites.youtrackdiscordbot;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.jda.JDACommandManager;
import com.google.inject.Inject;
import com.intellectualsites.youtrackdiscordbot.config.BotConfiguration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

class CommandManagerFactory {

    private final BotConfiguration configuration;

    @Inject CommandManagerFactory(final @NonNull BotConfiguration configuration) {
        this.configuration = configuration;
    }

    @NonNull JDACommandManager<MessageReceivedEvent> create(final @NonNull JDA jda) throws Exception {
        return new JDACommandManager<>(
                jda,
                c -> this.configuration.getPrefix(),
                (c, p) -> c.getMember().hasPermission(Permission.ADMINISTRATOR),
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity()
        );
    }

}
