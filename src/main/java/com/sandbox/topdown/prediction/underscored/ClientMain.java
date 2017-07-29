package com.sandbox.topdown.prediction.underscored;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import static com.sandbox.topdown.Game.VIEW_HEIGHT;
import static com.sandbox.topdown.Game.VIEW_WIDTH;
import java.io.IOException;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Maarten
 */
public class ClientMain extends Application {

    public static void main(String[] args) throws IOException {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Topdown (test)";
        config.useGL30 = true;
        config.width = VIEW_WIDTH;
        config.height = VIEW_HEIGHT;
        config.foregroundFPS = 144;

        LwjglApplication application = new LwjglApplication(ClientListener.getInstance(), config);

        ClientMain.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        CheckBox boxNaive = new CheckBox();
        CheckBox boxClientSmooth = new CheckBox();
        boxClientSmooth.setSelected(true);
        CheckBox boxClientPredict = new CheckBox();
        boxClientPredict.setSelected(true);

        CheckBox boxServerPos = new CheckBox();
        CheckBox boxDestPos = new CheckBox();

        boxNaive.selectedProperty().addListener((obs, old, newVal) -> ClientListener.getInstance().game.naive_approach = newVal);
        boxClientSmooth.selectedProperty().addListener((obs, old, newVal) -> ClientListener.getInstance().game.client_smoothing = newVal);
        boxClientPredict.selectedProperty().addListener((obs, old, newVal) -> ClientListener.getInstance().game.client_predict = newVal);
//        boxServerPos.selectedProperty().addListener((obs, old, newVal) -> ClientListener.getInstance().game.naive_approach = newVal);
//        boxDestPos.selectedProperty().addListener((obs, old, newVal) -> ClientListener.getInstance().game.naive_approach = newVal);

        GridPane root = new GridPane();
        root.setHgap(5);
        root.setVgap(5);
        root.addRow(0, createRow("Naive approach", boxNaive));
        root.addRow(1, createRow("Client smoothing", boxClientSmooth));
        root.addRow(2, createRow("Client prediction", boxClientPredict));
        root.addRow(3, createSpacer(10));
        root.addRow(4, createRow("Show server position", boxServerPos));
        root.addRow(5, createRow("Show dest position", boxDestPos));

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private Node[] createRow(String text, Node component) {
        return new Node[]{new Label(text), component};
    }

    private Node createSpacer(int height) {
        VBox box = new VBox();
        box.setMinHeight(height);
        return box;
    }

    static class ClientListener implements ApplicationListener {

        static ClientListener listener = new ClientListener();
        private ShapeRenderer shapeRenderer;
        public GameCore game;

        public static ClientListener getInstance() {
            return listener;
        }

        private ClientListener() {
        }

        @Override
        public void create() {
            this.shapeRenderer = new ShapeRenderer();
            this.game = new GameCore(null);
            this.game.update();
        }

        @Override
        public void resize(int width, int height) {
        }

        @Override
        public void render() {
            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

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
        }

    }

}
