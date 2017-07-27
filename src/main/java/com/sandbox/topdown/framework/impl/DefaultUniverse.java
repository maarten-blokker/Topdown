package com.sandbox.topdown.framework.impl;

import com.sandbox.topdown.framework.GameEntity;
import com.sandbox.topdown.framework.Universe;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Maarten
 */
public class DefaultUniverse implements Universe {

    private final List<GameEntity> entities = new CopyOnWriteArrayList<>();
    private final float width;
    private final float height;

    public DefaultUniverse(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public List<GameEntity> getEntities() {
        return entities;
    }

}
