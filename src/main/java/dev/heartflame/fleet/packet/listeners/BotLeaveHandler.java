package dev.heartflame.fleet.packet.listeners;

import dev.heartflame.fleet.util.HLogger;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;

public class BotLeaveHandler extends SessionAdapter {

    private final String USERNAME;

    public BotLeaveHandler(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        HLogger.error(String.format("Bot [%s] disconnected from the server with reason: [%s].", USERNAME, event.getCause().getMessage()));
    }


}
