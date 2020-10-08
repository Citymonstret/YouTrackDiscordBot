package com.intellectualsites.youtrackdiscordbot.config;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.intellectualsites.youtrackdiscordbot.annotations.BotDirectory;
import com.typesafe.config.ConfigRenderOptions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;

public class FileBotConfiguration implements BotConfiguration {

    private final File directory;
    private FileBotConfigurationObject fileBotConfigurationObject;

    @Inject
    public FileBotConfiguration(final @NonNull @BotDirectory File directory) {
        this.directory = directory;
        this.loadConfiguration();
    }

    public void loadConfiguration() {
        final File configFile = new File(this.directory, "bot.conf");
        final AbstractConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader
                .builder()
                .setRenderOptions(ConfigRenderOptions
                        .defaults()
                        .setComments(true)
                        .setFormatted(true)
                        .setOriginComments(false)
                        .setJson(false))
                .setDefaultOptions(ConfigurationOptions.defaults())
                .setFile(configFile)
                .build();

        FileBotConfigurationObject configurationObject = null;
        ConfigurationNode configurationNode;

        try {
            configurationNode = loader.load();
        } catch (final IOException e) {
            e.printStackTrace();
            configurationNode = loader.createEmptyNode();
        }

        if (!configFile.exists()) {
            configurationObject = new FileBotConfigurationObject();
            try {
                final CommentedConfigurationNode commentedConfigurationNode = loader.createEmptyNode();
                commentedConfigurationNode.setComment("");
                loader.save(commentedConfigurationNode.setValue(
                        TypeToken.of(FileBotConfigurationObject.class),
                        configurationObject
                ));
            } catch (final IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        } else {
            try {
                configurationObject = configurationNode.getValue(
                        TypeToken.of(FileBotConfigurationObject.class),
                        new FileBotConfigurationObject()
                );
            } catch (final ObjectMappingException e) {
                e.printStackTrace();
            }
        }

        this.fileBotConfigurationObject = configurationObject;
    }

    @Override
    public @NonNull String getToken() {
        return this.fileBotConfigurationObject.getToken();
    }

    @Override
    public @NonNull String getPrefix() {
        return this.fileBotConfigurationObject.getPrefix();
    }

}
