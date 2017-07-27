package com.sandbox.topdown.network.packet;

/**
 *
 * @author Maarten
 */
public class UpdateSessionCommand extends Packet {

    private String name;

    public UpdateSessionCommand() {
        super(ID_UPDATE_SESSION);
    }

    public UpdateSessionCommand(String name) {
        super(ID_UPDATE_SESSION);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
