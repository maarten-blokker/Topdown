package com.sandbox.topdown.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sandbox.topdown.framework.GameEntity;
import com.sandbox.topdown.framework.Graphics;
import com.sandbox.topdown.framework.RenderComponent;
import com.sandbox.topdown.framework.Universe;
import com.sandbox.topdown.framework.utils.MathUtil;

/**
 *
 * @author Maarten
 */
public class PlayerGraphics implements RenderComponent<PlayerEntity> {

    private Texture texture;
    private TextureRegion player;
    private SpriteBatch batch;
    private Camera cam;
    private FreeTypeFontGenerator generator;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    public PlayerGraphics(Camera cam) {
        this.cam = cam;
    }

    @Override
    public void init(Universe universe, PlayerEntity entity) {
        this.texture = new Texture(Gdx.files.classpath("assets/player.png"));
        this.player = new TextureRegion(texture);
        this.batch = new SpriteBatch();
        this.batch.setProjectionMatrix(cam.combined);
        this.shapeRenderer = new ShapeRenderer();
        this.shapeRenderer.setProjectionMatrix(cam.combined);

        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 12;
        this.generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/Prototype.ttf"));
        this.font = generator.generateFont(parameter);
    }

    @Override
    public void update(Universe container, PlayerEntity entity, int delta) {

    }

    @Override
    public void render(Universe container, PlayerEntity entity, Graphics g) {
        float x = entity.pos.x;
        float y = entity.pos.y;
        int w = texture.getWidth();
        int h = texture.getHeight();

        this.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        renderArm(entity, -70 + entity.leftFist);
        renderArm(entity, 70 - entity.rightFist);
        this.shapeRenderer.end();

        batch.begin();
        batch.draw(player, x - w / 2, y - w / 2, w / 2, h / 2, w, h, 1, 1, entity.direction);
        renderStats(entity);
        batch.end();

    }

    private void renderArm(GameEntity entity, float angle) {
        int dist = texture.getWidth() / 2 + 3;
        float armX = entity.pos.x + MathUtil.translateX(dist, entity.direction + angle);
        float armY = entity.pos.y + MathUtil.translateY(dist, entity.direction + angle);

        this.shapeRenderer.circle(armX, armY, 8);
    }

    private void renderStats(GameEntity entity) {
        font.draw(batch, "FPS = " + Gdx.graphics.getFramesPerSecond(), 5, 55);
        font.draw(batch, "Delta = " + Gdx.graphics.getDeltaTime(), 5, 45);
        font.draw(batch, "Player X = " + entity.pos.x, 5, 35);
        font.draw(batch, "Player Y = " + entity.pos.y, 5, 25);
        font.draw(batch, "Player Direction = " + entity.direction, 5, 15);

    }

    @Override
    public void dispose(Universe universe, PlayerEntity entity) {
        generator.dispose();
    }

}
