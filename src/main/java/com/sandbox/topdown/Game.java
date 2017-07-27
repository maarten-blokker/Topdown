package com.sandbox.topdown;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * A game using libgdx
 */
public class Game {

    public static final int VIEW_WIDTH = 1024;
    public static final int VIEW_HEIGHT = 768;
    public static float TIME_STEP = 1 / 45f;
    public static int VELOCITY_ITERATIONS = 6;
    public static int POSITION_ITERATIONS = 2;

    public static void main(String[] args) {
        Game game = new Game();
        game.run();

    }

    public Game() {

    }

    private void run() {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Topdown (test)";
        config.useGL30 = true;
        config.width = VIEW_WIDTH;
        config.height = VIEW_HEIGHT;
        config.foregroundFPS = 144;

        LwjglApplication application = new LwjglApplication(new GameListener(), config);
    }

}
