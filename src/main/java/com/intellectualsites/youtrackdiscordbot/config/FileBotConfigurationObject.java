package com.intellectualsites.youtrackdiscordbot.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

@ConfigSerializable
final class FileBotConfigurationObject {

    @Setting(comment = "Bot token")
    private String token = "your_token_here";

    @Setting(comment = "Bot prefix")
    private String prefix = "-yt";

    @NonNull String getToken() {
        return this.token;
    }

    @NonNull String getPrefix() {
        return this.prefix;
    }

}
