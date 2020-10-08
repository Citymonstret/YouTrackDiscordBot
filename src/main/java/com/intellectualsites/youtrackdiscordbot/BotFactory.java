package com.intellectualsites.youtrackdiscordbot;

import com.google.inject.Inject;
import com.intellectualsites.youtrackdiscordbot.config.BotConfiguration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.checkerframework.checker.nullness.qual.NonNull;

class BotFactory {

    private final BotConfiguration configuration;

    @Inject
    BotFactory(final @NonNull BotConfiguration configuration) {
        this.configuration = configuration;
    }

    @NonNull JDA createBot() throws Exception {
        return JDABuilder.create(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS
        ).setToken(this.configuration.getToken())
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setAutoReconnect(true)
                .build();
    }

}
