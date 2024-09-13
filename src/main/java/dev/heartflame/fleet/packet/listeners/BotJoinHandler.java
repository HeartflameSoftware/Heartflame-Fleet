package dev.heartflame.fleet.packet.listeners;

import dev.heartflame.fleet.util.HLogger;
import org.geysermc.mcprotocollib.network.event.session.ConnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;

public class BotJoinHandler extends SessionAdapter {
    private final String USERNAME;

    public BotJoinHandler(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    @Override
    public void connected(ConnectedEvent event) {
        HLogger.info(String.format("Bot [%s] successfully connected to server.", USERNAME));
    }
}
