package com.sandbox.topdown.prediction.underscored;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class GameInput {

    private static final Logger LOG = LoggerFactory.getLogger(GameInput.class);
    public int seq;
    public float time;

    public boolean up;
    public boolean down;
    public boolean left;
    public boolean right;

    public GameInput() {
    }

    public GameInput(GameInput input) {
        this.seq = input.seq;
        this.time = input.time;
        this.up = input.up;
        this.down = input.down;
        this.left = input.left;
        this.right = input.right;
    }

    public GameInput(int seq, float time, boolean up, boolean down, boolean left, boolean right) {
        this.seq = seq;
        this.time = time;
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "GameInput{" + "seq=" + seq + ", up=" + up + ", down=" + down + ", left=" + left + ", right=" + right + '}';
    }

}
