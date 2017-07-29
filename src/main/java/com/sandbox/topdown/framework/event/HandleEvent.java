package com.sandbox.topdown.framework.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be used in conjunction with a method to give an
 * alternative way to register handlers for events
 *
 * @author Maarten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HandleEvent {
    Class<? extends Event> value();
}
