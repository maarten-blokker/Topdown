//package com.sandbox.topdown.network.server;
//
//import com.sandbox.topdown.network.client.GameClient;
//import com.sandbox.topdown.network.packet.Packet;
//import com.sandbox.topdown.network.packet.UpdateSessionCommand;
//import com.sandbox.topdown.network.packet.WelcomePacket;
//import java.net.InetSocketAddress;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.function.Predicate;
//import org.junit.After;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// *
// * @author Maarten
// */
//public class GameServerHandlerTest {
//
//    private static final int PORT = 9001;
//    private GameServer server;
//
//    @Before
//    public void setUp() throws Exception {
//        this.server = new GameServer();
//        this.server.listen(PORT);
//    }
//
//    @After
//    public void tearDown() {
//        this.server.stop();
//    }
//
//    @Test(timeout = 1000L)
//    public void testWelcomePacket() throws Exception {
//        GameClient client = new GameClient();
//        client.connect(new InetSocketAddress("localhost", PORT));
//
//        Packet packet = waitForPacket(client, (p) -> {
//            return p instanceof WelcomePacket;
//        });
//
//        assertNotNull(packet);
//    }
//
//    @Test
//    public void testUpdateSessionCommand() throws Exception {
//        String user = "Test user";
//
//        GameClient client = new GameClient();
//        client.connect(new InetSocketAddress("localhost", PORT));
//        client.send(new UpdateSessionCommand(user));
//
//        Thread.sleep(500);
//
//        List<PlayerSession> sessions = server.getSessions();
//        assertEquals(1, sessions.size());
//        assertEquals(user, sessions.get(0).getName());
//    }
//
//    private Packet waitForPacket(GameClient client, Predicate<Packet> filter) {
//        CountDownLatch latch = new CountDownLatch(1);
//        AtomicReference<Packet> packet = new AtomicReference<>();
//        client.addPacketListener((p) -> {
//            if (filter.test(p)) {
//                packet.set(p);
//                latch.countDown();
//            }
//        });
//
//        try {
//            latch.await();
//        } catch (InterruptedException ex) {
//            fail();
//        }
//
//        return packet.get();
//    }
//
//}
