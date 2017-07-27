package com.sandbox.topdown.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.sandbox.topdown.framework.Component;
import com.sandbox.topdown.framework.GameEntity;
import com.sandbox.topdown.framework.Universe;
import com.sandbox.topdown.framework.utils.MathUtil;

/**
 *
 * @author Maarten
 */
public class PlayerPhysics implements Component {

    private static final float MAX_VELOCITY = 100000F;

    private final float width;
    private final float height;
    private final World world;
    private CircleShape shape;
    private Body body;

    public PlayerPhysics(float width, float height, World world) {
        this.width = width;
        this.height = height;
        this.world = world;
    }

    @Override
    public void init(Universe universe, GameEntity entity) {
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set our body's starting position in the world
        bodyDef.position.set(entity.x, entity.y);

        // Create our body in the world using our body definition
        this.body = world.createBody(bodyDef);
        // Apply a force of 1 meter per second on the X-axis at pos.x/pos.y of the body slowly moving it right
        this.body.getMassData().mass = 80;

        // Create a circle shape and set its radius to 6
        this.shape = new CircleShape();
        this.shape.setRadius(width);
        this.shape.setPosition(new Vector2(width / 2, height / 2));

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0f; // Make it bounce a little bit
        this.body.setLinearDamping(1f);

        // Create our fixture and attach it to the body
        body.createFixture(fixtureDef);

        System.out.println(body.getMass());
    }

    @Override
    public void update(Universe universe, GameEntity entity, int delta) {
        Vector2 vel = this.body.getLinearVelocity();
        Vector2 pos = this.body.getWorldCenter();

        float acceleration = 12000f;

        if (Gdx.input.isKeyPressed(Input.Keys.A) && vel.x > -MAX_VELOCITY) {
            this.body.applyForce(-acceleration, 0, pos.x, pos.y, true);
        } else if (Gdx.input.isKeyPressed(Input.Keys.D) && vel.x < MAX_VELOCITY) {
            this.body.applyForce(acceleration, 0, pos.x, pos.y, true);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W) && vel.y > -MAX_VELOCITY) {
            this.body.applyForce(0f, acceleration, pos.x, pos.y, true);
        } else if (Gdx.input.isKeyPressed(Input.Keys.S) && vel.y < MAX_VELOCITY) {
            this.body.applyForce(0f, -acceleration, pos.x, pos.y, true);
        }

//        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
//            entity.y += speed;
//        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
//            entity.y -= speed;
//        }
//
//        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
//            entity.direction += speed;
//        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
//            entity.direction -= speed;
//        }
        entity.x = pos.x;
        entity.y = pos.y;
        entity.direction = MathUtil.radiansToDegrees(this.body.getAngle());

    }

    @Override
    public void dispose(Universe universe, GameEntity entity) {
        this.shape.dispose();
    }

}
