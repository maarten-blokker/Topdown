package com.sandbox.topdown.prediction.underscored;

import com.badlogic.gdx.math.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class ServerUpdate {

    private static final Logger LOG = LoggerFactory.getLogger(ServerUpdate.class);

    public Vector2 clientPosition;
    public Vector2 hostPosition;
    public int clientInputSequence;
    public int hostInputSequence;
    public float time;

    public ServerUpdate() {
    }

}
