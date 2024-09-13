package dev.heartflame.fleet.packet;

import dev.heartflame.fleet.packet.listeners.BotJoinHandler;
import dev.heartflame.fleet.packet.listeners.BotLeaveHandler;
import org.geysermc.mcprotocollib.network.Session;

public class PacketHandler {

    // All packet handlers registered here have the client session passed to them, for them to listen for packets.
    public PacketHandler(Session client, String BOT_USERNAME) {

        client.addListener(new BotJoinHandler(BOT_USERNAME));
        client.addListener(new BotLeaveHandler(BOT_USERNAME));

    }
}
