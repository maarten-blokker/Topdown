package com.sandbox.topdown.prediction.underscored.packet;

import com.sandbox.topdown.prediction.underscored.ServerUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class ServerUpdatePacket extends Packet {

    private static final Logger LOG = LoggerFactory.getLogger(ServerUpdatePacket.class);

    private ServerUpdate update;

    public ServerUpdatePacket() {
        super(Type.UPDATE);
    }

    public ServerUpdatePacket(ServerUpdate update) {
        super(Type.UPDATE);
        this.update = update;
    }

    public ServerUpdate getUpdate() {
        return update;
    }

    public void setUpdate(ServerUpdate update) {
        this.update = update;
    }

    @Override
    public String toString() {
        return "ServerUpdatePacket{" + "update=" + update + '}';
    }

}
