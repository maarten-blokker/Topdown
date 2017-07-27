package com.sandbox.topdown.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Maarten
 */
public class GameEntity {

    public float x;
    public float y;
    public float direction;

    private final List<Component> components = new ArrayList<>();
    private final List<RenderComponent> renderComponents = new ArrayList<>();

    private final List<Component> readComponents = Collections.unmodifiableList(components);
    private final List<RenderComponent> readRenderComponents = Collections.unmodifiableList(renderComponents);

    public GameEntity() {
    }

    public final void addComponent(Component component) {
        components.add(component);
        if (component instanceof RenderComponent) {
            renderComponents.add((RenderComponent) component);
        }
    }

    public final void removeComponent(Component component) {
        components.remove(component);
        renderComponents.remove(component);
    }

    public List<Component> getComponents() {
        return readComponents;
    }

    public List<RenderComponent> getRenderComponents() {
        return readRenderComponents;
    }

}
