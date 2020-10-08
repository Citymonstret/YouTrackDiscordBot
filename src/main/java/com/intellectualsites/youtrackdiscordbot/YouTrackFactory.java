package com.intellectualsites.youtrackdiscordbot;

import com.google.inject.Inject;
import com.intellectualsites.youtrackdiscordbot.config.BotConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.llorllale.youtrack.api.DefaultYouTrack;
import org.llorllale.youtrack.api.YouTrack;
import org.llorllale.youtrack.api.session.Anonymous;

import java.net.URL;

class YouTrackFactory {

    private final BotConfiguration configuration;

    @Inject
    YouTrackFactory(final @NonNull BotConfiguration configuration) {
        this.configuration = configuration;
    }

    @NonNull YouTrack create() throws Exception {
        return new DefaultYouTrack(
               new Anonymous(new URL(this.configuration.getYouTrackURL() + "/rest"))
        );
    }

}
