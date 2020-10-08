package com.intellectualsites.youtrackdiscordbot.config;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Bot configuration
 */
public interface BotConfiguration {

    /**
     * (Re-)load the configuration
     */
    void loadConfiguration();

    /**
     * Get the bot token
     *
     * @return Bot token
     */
    @NonNull String getToken();

    /**
     * Get the bot prefix
     *
     * @return Bot prefix
     */
    @NonNull String getPrefix();

}
