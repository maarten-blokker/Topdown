package com.sandbox.topdown.prediction.underscored.packet;

import com.sandbox.topdown.prediction.underscored.GameInput;

/**
 *
 * @author Maarten
 */
public class InputPacket extends Packet {


    private int inputSequence;
    private float localTime;
    private GameInput input;

    public InputPacket() {
        super(Type.INPUT);
    }

    public InputPacket(int inputSequence, float localTime, GameInput input) {
        super(Type.INPUT);
        this.inputSequence = inputSequence;
        this.localTime = localTime;
        this.input = input;
    }

    public int getInputSequence() {
        return inputSequence;
    }

    public void setInputSequence(int inputSequence) {
        this.inputSequence = inputSequence;
    }

    public float getLocalTime() {
        return localTime;
    }

    public void setLocalTime(float localTime) {
        this.localTime = localTime;
    }

    public GameInput getInput() {
        return input;
    }

    public void setInput(GameInput input) {
        this.input = input;
    }

}
