package com.sandbox.topdown.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.sandbox.topdown.framework.Component;
import com.sandbox.topdown.framework.Universe;
import com.sandbox.topdown.framework.utils.MathUtil;

/**
 *
 * @author Maarten
 */
public class PlayerControls implements Component<PlayerEntity> {

    private final float speed = 2;

    private final Camera cam;

    public PlayerControls(Camera cam) {
        this.cam = cam;
    }

    @Override
    public void init(Universe universe, PlayerEntity entity) {

    }

    @Override
    public void update(Universe container, PlayerEntity entity, int delta) {
        Vector3 mouse = new Vector3();
        cam.unproject(mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        entity.direction = MathUtil.directionToPoint(entity.pos.x, entity.pos.y, mouse.x, mouse.y);

        updateMovement(entity);
        updateFists(entity);
    }

    @Override
    public void dispose(Universe universe, PlayerEntity entity) {
    }

    private void updateMovement(PlayerEntity entity) {
        float dstX = 0;
        float dstY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            dstX -= speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            dstX += speed;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            dstY -= speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            dstY += speed;
        }

        float targetDirection = MathUtil.directionToPoint(0, 0, dstX, dstY);
        float targetDistance = (dstX == 0 && dstY == 0) ? 0 : speed;

        entity.pos.x = entity.pos.x + MathUtil.translateX(targetDistance, targetDirection);
        entity.pos.y = entity.pos.y + MathUtil.translateY(targetDistance, targetDirection);

    }

    private void updateFists(PlayerEntity entity) {

        if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
            entity.rightFist = Math.min(50, entity.rightFist + 5);
        } else if (entity.rightFist > 0) {
            entity.rightFist = Math.max(0, entity.rightFist - 5);
        }

        if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
            entity.leftFist = Math.min(50, entity.leftFist + 5);
        } else if (entity.leftFist > 0) {
            entity.leftFist = Math.max(0, entity.leftFist - 5);
        }
    }

}
