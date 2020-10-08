package com.intellectualsites.youtrackdiscordbot.config;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;

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

    /**
     * Get the URL pointing to the YouTrack instance
     *
     * @return YouTrack URL
     */
    @NonNull String getYouTrackURL();

    /**
     * Get the YouTrack authentication token
     *
     * @return YouTrack token
     */
    @NonNull String getYouTrackToken();

    /**
     * Get the YouTrack project names
     *
     * @return Youtrack Project names
     */
    @NonNull List<String> getYouTrackProjects();

    /**
     * Get the broadcast channels
     *
     * @return Channels
     */
    @NonNull Map<String, Long> getBroadcastChannels();

}
