package com.sandbox.topdown.prediction.underscored;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import static com.sandbox.topdown.Game.VIEW_HEIGHT;
import static com.sandbox.topdown.Game.VIEW_WIDTH;
import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Maarten
 */
public class ClientMain extends Application {

    public static void main(String[] args) throws IOException {
        ClientMain.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        DebugController controller = new DebugController();

        primaryStage.setScene(new Scene(controller.getView()));
        primaryStage.show();

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Topdown (test)";
        config.useGL30 = true;
        config.width = VIEW_WIDTH;
        config.height = VIEW_HEIGHT;
        config.foregroundFPS = 144;

        ClientListener listener = new ClientListener(controller);
        LwjglApplication application = new LwjglApplication(listener, config);
    }

    static class ClientListener implements ApplicationListener {

        private final DebugController controller;
        
        private SpriteBatch batch;
        private ShapeRenderer shapeRenderer;
        private FreeTypeFontGenerator generator;
        private BitmapFont font;
        public GameCore game;

        private ClientListener(DebugController controller) {
            this.controller = controller;
        }

        @Override
        public void create() {
            this.batch = new SpriteBatch();
            this.shapeRenderer = new ShapeRenderer();
            this.game = new GameCore(null);
            this.game.update();

            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 10;
            this.generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/Prototype.ttf"));
            this.font = generator.generateFont(parameter);

            controller.initComponents(game);
        }

        @Override
        public void resize(int width, int height) {
        }

        @Override
        public void render() {
            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

            batch.begin();
            if (game.self != null && game.self.state != null) {

                font.draw(batch, "Ping = " + game.net_ping, 5, 40);
                font.draw(batch, "Local time = " + game.local_time, 5, 30);
                font.draw(batch, "State = " + game.self.state.name(), 5, 20);
            }

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.identity();
            if (game.self != null) {
                renderCircle(game.self.pos, 16, game.self.color);
            }
            if (game.self != null) {
                renderCircle(game.self.pos, 16, game.self.color);
            }

            if (game.other != null) {
                renderCircle(game.other.pos, 16, game.other.color);
            }

            batch.end();
            shapeRenderer.end();
        }

        private void renderCircle(Vector2 position, int radius, Color color) {
            if (color == null) {
                color = Color.WHITE;
            }

            shapeRenderer.setColor(color);
            shapeRenderer.circle(position.x, position.y, radius);
        }

        @Override
        public void pause() {
        }

        @Override
        public void resume() {
        }

        @Override
        public void dispose() {
            this.generator.dispose();
        }

    }

}
