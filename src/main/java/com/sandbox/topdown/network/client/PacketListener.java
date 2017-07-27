package com.sandbox.topdown.network.client;

import com.sandbox.topdown.network.packet.Packet;

/**
 *
 * @author Maarten
 */
public interface PacketListener {
    void onPacket(Packet packet);
}
