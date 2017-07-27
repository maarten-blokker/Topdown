package com.sandbox.topdown.framework;

/**
 *
 * @author Maarten
 * @param <E> The type of entity this component renders
 */
public interface RenderComponent<E extends GameEntity> extends Component<E> {

    void render(Universe universe, E entity, Graphics g);
}
