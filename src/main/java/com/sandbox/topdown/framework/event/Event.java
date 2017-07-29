package com.sandbox.topdown.framework.event;

/**
 *
 * @author Maarten
 */
public class Event {

    private Object source;
    private boolean consumed;

    public Event() {
        this.source = null;
    }

    public Event(Object source) {
        this.source = source;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        this.consumed = true;
    }

    @Override
    public String toString() {
        return "Event{" + "source=" + source + ", consumed=" + consumed + '}';
    }

}
