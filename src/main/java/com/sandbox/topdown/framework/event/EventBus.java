package com.sandbox.topdown.framework.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Maarten
 */
public class EventBus {

    private final Map<Class<? extends Event>, List<EventHandler<? super Event>>> handlers
            = Collections.synchronizedMap(new HashMap<>());

    /**
     * Publish an event on the {@link EventBus}, notifying every event handler
     * that is subscribed to this type of event. Handlers that handle a subclass
     * of this event get notified too.
     *
     * @param evt The event to notify all handlers of.
     * @return Returns this instance of the {@link EventBus EventBus}, to allow
     * daisy chaining.
     */
    public EventBus publish(Event evt) {
        if (evt != null) {
            Class<? extends Event> eventClass = evt.getClass();
            while (Event.class.isAssignableFrom(eventClass) && !evt.isConsumed()) {
                List<EventHandler<? super Event>> eventHandlers = this.handlers.get(eventClass);
                if (eventHandlers != null) {
                    for (EventHandler<? super Event> handler : new ArrayList<>(eventHandlers)) {
                        if (evt.isConsumed()) {
                            break;
                        }

                        handler.handle(evt);
                    }
                }

                eventClass = (Class<? extends Event>) eventClass.getSuperclass();
                if (eventClass.equals(Event.class)) {
                    break;
                }
            }
        }

        return this;
    }

    /**
     * Subscribe an {@link EventHandler event handler} for the given event
     * class. A handler can be subscribed for a subclass of a specific
     * {@link Event event}. The event handlers of a specific event class are
     * called handled first, then the sub classed event handlers
     *
     * @param <T> The type of the event
     * @param eventClass The event class
     * @param handler The handler that handles the event when it gets
     * {@link #publish(nl.caliope.framework.events.Event) published}.
     * @return Returns this instance of the {@link EventBus EventBus}, to allow
     * daisy chaining.
     */
    public <T extends Event> EventBus subscribe(Class<T> eventClass, EventHandler<T> handler) {
        List<EventHandler<? super Event>> eventHandlers = this.handlers.get(eventClass);
        if (eventHandlers == null) {
            eventHandlers = new ArrayList<>();
        } else {
            eventHandlers = new ArrayList<>(eventHandlers);
        }
        eventHandlers.add((EventHandler<? super Event>) handler);

        this.handlers.put(eventClass, eventHandlers);
        return this;
    }

    /**
     * Subscribes event handlers using the {@link HandleEvent HandleEvent}
     * annotation. This method works the same way as it would with
     * {@link #subscribe(java.lang.Class, nl.caliope.framework.events.EventHandler)},
     * with the exception that it throws an exception if no annotated methods
     * can be found. Annotated methods must have either no arguments, or 1
     * argument, capturing the instance of the event specified in the
     * annotation.
     *
     * @param handlerContainer
     */
    public void subscribe(Object handlerContainer) {
        Method[] methods = handlerContainer.getClass().getMethods();
        boolean found = false;
        for (Method method : methods) {
            HandleEvent annotation = findAnnotation(method);
            if (annotation == null) {
                continue;
            }

            //verify that this method is in a correct notation, throws an exception if it's not.
            verifyAnnotation(method, annotation);

            Class<? extends Event> eventClass = annotation.value();
            int argCount = method.getParameterCount();
            found = true;
            subscribe(eventClass, (evt) -> {
                try {
                    if (argCount == 0) {
                        method.invoke(handlerContainer);
                    } else {
                        method.invoke(handlerContainer, evt);
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        if (!found) {
            throw new IllegalArgumentException("No valid methods annotated with @HandleEvent found");
        }
    }

    /**
     * @param <T> The type of the event
     * @param eventClass The event class
     * @param handler The handler to remove from this event bus
     * @return returns true if the handler was successfully removed, or false if
     * no handler was found to remove.
     */
    public <T extends Event> boolean unsubscribe(Class<T> eventClass, EventHandler<T> handler) {
        boolean removed = false;

        List<EventHandler<? super Event>> eventHandlers = this.handlers.get(eventClass);
        if (eventHandlers != null) {
            EventHandler<? super Event> eventHandler = (EventHandler<? super Event>) handler;
            eventHandlers = new ArrayList<>(eventHandlers);
            removed = eventHandlers.remove(eventHandler);
            this.handlers.put(eventClass, eventHandlers);
        }

        return removed;
    }

    private HandleEvent findAnnotation(Method method) {
        HandleEvent annotation = method.getAnnotation(HandleEvent.class);
        Class<?> cl = method.getDeclaringClass();
        while (annotation == null) {
            cl = cl.getSuperclass();
            if (cl == null || cl.equals(Object.class)) {
                break;
            }

            try {
                Method equivalentMethod = cl.getDeclaredMethod(method.getName(), method.getParameterTypes());
                annotation = equivalentMethod.getAnnotation(HandleEvent.class);
            } catch (NoSuchMethodException ex) {
                // We're done...
            }
        }

        return annotation;
    }

    private void verifyAnnotation(Method method, HandleEvent annotation) {
        Class<? extends Event> eventClass = annotation.value();
        int argCount = method.getParameterCount();

        if (argCount > 1) {
            throw new IllegalArgumentException(
                    "invalid number of arguments " + argCount
            );
        } else if (argCount == 1) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (!eventClass.equals(parameterTypes[0])) {
                throw new IllegalArgumentException(
                        String.format("Parameter is of wrong type, expected (%s) but have (%s)",
                                eventClass.toString(), parameterTypes[0].toString())
                );
            }
        }
    }
}
