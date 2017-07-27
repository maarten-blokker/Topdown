package com.sandbox.topdown.framework;

import java.util.List;

/**
 *
 * @author Maarten
 */
public interface Universe {

    List<GameEntity> getEntities();

    float getWidth();

    float getHeight();

}
