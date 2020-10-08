package com.intellectualsites.youtrackdiscordbot.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Listens to incoming messages in order to execute commands
 */
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
        super.onMessageReceived(event);
    }

}
