package com.sandbox.topdown.framework;

/**
 *
 * @author Maarten
 * @param <E> The type of entity this component renders
 */
public interface Component<E extends GameEntity> {

    void init(Universe universe, E entity);

    void update(Universe universe, E entity, int delta);

    void dispose(Universe universe, E entity);
}
