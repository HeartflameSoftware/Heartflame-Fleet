package dev.heartflame.fleet.packet.listeners;

import org.geysermc.mcprotocollib.network.event.session.ConnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotJoinHandler extends SessionAdapter {

    private static final Logger log = LoggerFactory.getLogger("Bot Actions");

    private final String USERNAME;

    public BotJoinHandler(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    @Override
    public void connected(ConnectedEvent event) {
        log.info("{} successfully connected to server.", USERNAME);
    }
}
