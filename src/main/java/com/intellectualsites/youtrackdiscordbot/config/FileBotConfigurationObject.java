package com.intellectualsites.youtrackdiscordbot.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
final class FileBotConfigurationObject {

    @Setting(comment = "Bot token")
    private String token = "your_token_here";

    @Setting(comment = "Bot prefix")
    private String prefix = "-yt";

    @Setting(comment = "YouTrack URL")
    private String youTrackURL = "https://your.url/here";

    @Setting(comment = "YouTrack token")
    private String youTrackToken = "your_token_here";

    @Setting(comment = "YouTrack project names")
    private List<String> youTrackProjects = Collections.singletonList("your_project");

    @Setting(comment = "Broadcast channel IDs")
    private Map<String, Long> youTrackChannels = new HashMap<String, Long>() {
        {
            put("channel", -1L);
        }
    };

    @NonNull String getToken() {
        return this.token;
    }

    @NonNull String getPrefix() {
        return this.prefix;
    }

    @NonNull String getYouTrackURL() {
        return this.youTrackURL;
    }

    @NonNull String getYouTrackToken() {
        return this.youTrackToken;
    }

    @NonNull List<@NonNull String> getYouTrackProjects() {
        return this.youTrackProjects;
    }

    @NonNull Map<String, Long> getBroadcastChannels() {
        return this.youTrackChannels;
    }

}
