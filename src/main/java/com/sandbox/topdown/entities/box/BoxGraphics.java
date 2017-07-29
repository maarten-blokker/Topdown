package com.sandbox.topdown.entities.box;

import com.sandbox.topdown.framework.GameEntity;
import com.sandbox.topdown.framework.Graphics;
import com.sandbox.topdown.framework.Universe;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sandbox.topdown.framework.RenderComponent;

/**
 *
 * @author Maarten
 */
public class BoxGraphics implements RenderComponent {

    private final int width;
    private final int height;
    private final Camera cam;
    private ShapeRenderer shapeRenderer;

    public BoxGraphics(int width, int height, Camera cam) {
        this.width = width;
        this.height = height;
        this.cam = cam;
    }

    @Override
    public void init(Universe universe, GameEntity entity) {
        this.shapeRenderer = new ShapeRenderer();
        this.shapeRenderer.setProjectionMatrix(cam.combined);
    }

    @Override
    public void update(Universe universe, GameEntity entity, int delta) {
    }

    @Override
    public void dispose(Universe universe, GameEntity entity) {
    }

    @Override
    public void render(Universe universe, GameEntity entity, Graphics g) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.identity();
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rotate(width/2, height/2, 1, entity.direction);
        shapeRenderer.rect(entity.pos.x, entity.pos.y, width, height);
        shapeRenderer.end();
    }

}
