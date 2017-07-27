package com.sandbox.topdown;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.sandbox.topdown.framework.GameEntity;
import com.sandbox.topdown.framework.Universe;
import com.sandbox.topdown.framework.impl.DefaultUniverse;
import com.sandbox.topdown.entities.box.BoxGraphics;
import com.sandbox.topdown.entities.player.PlayerControls;
import com.sandbox.topdown.entities.player.PlayerEntity;
import com.sandbox.topdown.entities.player.PlayerGraphics;
import com.sandbox.topdown.network.client.GameClient;
import com.sandbox.topdown.network.packet.Packet;
import com.sandbox.topdown.network.packet.UpdatePositionCommand;
import com.sandbox.topdown.network.packet.UpdateSessionCommand;
import com.sandbox.topdown.network.packet.WelcomePacket;
import com.sandbox.topdown.network.packet.event.PositionEvent;
import com.sandbox.topdown.network.packet.event.SessionEvent;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class GameListener extends ApplicationAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(GameListener.class);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Universe universe = new DefaultUniverse(800, 600);
    private OrthographicCamera cam;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Texture background;
    private SpriteBatch batch;
    private GameClient client;

    public GameListener() {

    }

    private void loadEntities() {
        PlayerEntity player = new PlayerEntity(cam);
        player.addComponent(new PlayerControls(cam));
        player.x = 300;
        player.y = 300;

        GameEntity box = new GameEntity();
        box.addComponent(new BoxGraphics(64, 64, cam));
        box.x = 400;
        box.y = 300;

        universe.getEntities().add(player);
        universe.getEntities().add(box);

        joinGame(player);
    }

    private void joinGame(GameEntity player) {

        Map<String, GameEntity> remotePlayers = new HashMap<>();
        this.client = new GameClient();
        this.client.addPacketListener((packet) -> {
            switch (packet.getId()) {
                case Packet.ID_EVENT_SESSION:
                    SessionEvent sessEvt = (SessionEvent) packet;
                    String sessionId = sessEvt.getSession().getId();
                    if (sessEvt.getType() == SessionEvent.TYPE_CREATED) {
                        GameEntity entity = new PlayerEntity(cam);
                        Gdx.app.postRunnable(() -> {
                            entity.getComponents().forEach((c) -> c.init(universe, entity));
                            universe.getEntities().add(entity);
                        });

                        remotePlayers.put(sessionId, entity);
                    } else if (sessEvt.getType() == SessionEvent.TYPE_DELETED) {
                        GameEntity entity = remotePlayers.remove(sessionId);
                        if (entity != null) {
                            universe.getEntities().add(entity);
                        }
                    }
                    break;
                case Packet.ID_EVENT_POSITION:
                    PositionEvent posEvt = (PositionEvent) packet;
                    GameEntity entity = remotePlayers.get(posEvt.getSessionId());
                    entity.x = posEvt.getX();
                    entity.y = posEvt.getY();
                    entity.direction = posEvt.getDirection();
                    break;
                case Packet.ID_JOIN:
                    LOG.info("Server: " + ((WelcomePacket) packet).getMessage());
                    break;
            }
        });
        client.connect(new InetSocketAddress("localhost", 9000));
        client.send(new UpdateSessionCommand("Terraego"));

        executor.scheduleAtFixedRate(() -> {
            this.client.send(new UpdatePositionCommand(player.x, player.y, player.direction));
        }, 10, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public void create() {
        cam = new OrthographicCamera(Game.VIEW_WIDTH, Game.VIEW_HEIGHT);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();

        this.world = new World(new Vector2(0, 0), true);
        this.debugRenderer = new Box2DDebugRenderer();

        loadEntities();

        universe.getEntities().forEach((entity) -> {
            entity.getComponents().forEach((c) -> c.init(universe, entity));
        });

        background = new Texture(Gdx.files.classpath("assets/textures/terrain/grass.png"));
        batch = new SpriteBatch();
        batch.setProjectionMatrix(cam.combined);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        batch.begin();
        for (int x = 0; x < Game.VIEW_WIDTH; x += background.getWidth()) {
            for (int y = 0; y < Game.VIEW_HEIGHT; y += background.getHeight()) {
                batch.draw(background, x, y);
            }
        }
        batch.end();

        cam.update();
        universe.getEntities().forEach((entity) -> {
            entity.getComponents().forEach((c) -> c.update(universe, entity, 0));
            entity.getRenderComponents().forEach((c) -> c.render(universe, entity, null));
        });

        debugRenderer.render(world, cam.combined);

    }

    private float accumulator;

    private void doPhysicsStep(float deltaTime) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= Game.TIME_STEP) {
            world.step(Game.TIME_STEP, Game.VELOCITY_ITERATIONS, Game.POSITION_ITERATIONS);
            accumulator -= Game.TIME_STEP;
        }
    }
}
