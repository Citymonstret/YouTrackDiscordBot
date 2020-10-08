package com.intellectualsites.youtrackdiscordbot.modules;

import com.google.inject.AbstractModule;
import com.intellectualsites.youtrackdiscordbot.YouTrackDiscordBot;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BotModule extends AbstractModule {

    private final YouTrackDiscordBot mainInstance;

    public BotModule(final @NonNull YouTrackDiscordBot mainInstance) {
        this.mainInstance = mainInstance;
    }

    @Override
    protected void configure() {
        this.bind(YouTrackDiscordBot.class).toInstance(this.mainInstance);
    }

}
