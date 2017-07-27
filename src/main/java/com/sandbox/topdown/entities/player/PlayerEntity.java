package com.sandbox.topdown.entities.player;

import com.badlogic.gdx.graphics.Camera;
import com.sandbox.topdown.framework.GameEntity;

/**
 *
 * @author Maarten
 */
public class PlayerEntity extends GameEntity {

    public int leftFist;
    public int rightFist;

    public PlayerEntity(Camera cam) {
        addComponent(new PlayerGraphics(cam));
    }

}
