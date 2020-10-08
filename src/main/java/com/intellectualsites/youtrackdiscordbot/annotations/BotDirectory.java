package com.intellectualsites.youtrackdiscordbot.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an injected {@link java.io.File}
 * should point to the bot directory
 */
@BindingAnnotation
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface BotDirectory {
}
