package dev.heartflame.fleet.bot;

import dev.heartflame.fleet.packet.PacketHandler;
import org.geysermc.mcprotocollib.auth.SessionService;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.tcp.TcpClientSession;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;

public class BotSession {

    public void newSession(String IP, int PORT, String BOT_USERNAME) {

        // Here, we only assign the bot username as there is no other authentication required.
        MinecraftProtocol protocol;
        protocol = new MinecraftProtocol(BOT_USERNAME);

        SessionService sessionService = new SessionService();

        Session client = new TcpClientSession(
                IP, // Server IP
                PORT, // Port of server
                protocol,
                null);
        client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService); // Add the session service.

        // All packets from all bots are handled here.
        PacketHandler packetHandler = new PacketHandler(client, BOT_USERNAME);

        client.connect();
    }
}
