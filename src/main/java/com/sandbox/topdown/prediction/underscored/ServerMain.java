package com.sandbox.topdown.prediction.underscored;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class ServerMain {

    private static final Logger LOG = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) throws Exception {
        GameServer game_server = new GameServer();

        ServerBootstrapper bootstapper = new ServerBootstrapper(game_server);
        bootstapper.listen(4004);

    }

}
