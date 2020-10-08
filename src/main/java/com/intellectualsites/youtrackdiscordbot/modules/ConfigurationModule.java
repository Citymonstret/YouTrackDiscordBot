package com.intellectualsites.youtrackdiscordbot.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.intellectualsites.youtrackdiscordbot.annotations.BotDirectory;
import com.intellectualsites.youtrackdiscordbot.config.BotConfiguration;
import com.intellectualsites.youtrackdiscordbot.config.FileBotConfiguration;

import java.io.File;

/**
 * Injection module binding configurations
 */
public class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        final File file = new File("./ytbot");
        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new IllegalStateException("Could not create bot directory");
            }
        }
        this.bind(File.class).annotatedWith(BotDirectory.class).toInstance(file);
        this.bind(BotConfiguration.class).to(FileBotConfiguration.class).in(Singleton.class);
    }

}
