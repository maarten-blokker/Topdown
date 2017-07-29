package com.sandbox.topdown.framework.event;

/**
 *
 * @author Maarten
 * @param <T>
 */
@FunctionalInterface
public interface EventHandler<T extends Event> {
    
    void handle(T event);
}

