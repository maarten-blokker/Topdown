package com.sandbox.topdown.entities.box;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.sandbox.topdown.framework.Component;
import com.sandbox.topdown.framework.GameEntity;
import com.sandbox.topdown.framework.Universe;
import com.sandbox.topdown.framework.utils.MathUtil;

/**
 *
 * @author Maarten
 */
public class BoxPhysics implements Component {

    private final float width;
    private final float height;
    private final World world;
    private PolygonShape shape;
    private Body body;

    public BoxPhysics(float width, float height, World world) {
        this.width = width;
        this.height = height;
        this.world = world;
    }

    @Override
    public void init(Universe universe, GameEntity entity) {
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyType.DynamicBody;
        // Set our body's starting position in the world
        bodyDef.position.set(entity.x + width / 2, entity.y + width / 2);

        // Create our body in the world using our body definition
        this.body = world.createBody(bodyDef);
        this.body.setLinearDamping(2f);
        this.body.setAngularDamping(3f);
        

        // Create a circle shape and set its radius to 6
        this.shape = new PolygonShape();
        this.shape.setAsBox(width / 2, height / 2);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = .3f;
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 0.0f; // Make it bounce a little bit

        
        // Create our fixture and attach it to the body
        Fixture fixture = body.createFixture(fixtureDef);

    }

    @Override
    public void update(Universe universe, GameEntity entity, int delta) {
        Vector2 pos = this.body.getPosition();
        entity.x = pos.x;
        entity.y = pos.y;
        entity.direction = MathUtil.radiansToDegrees(this.body.getAngle());
    }

    @Override
    public void dispose(Universe universe, GameEntity entity) {
        this.shape.dispose();
    }

}
