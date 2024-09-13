package dev.heartflame.fleet.socket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.heartflame.fleet.bot.BotHandler;
import dev.heartflame.fleet.crypto.HCrypto;
import dev.heartflame.fleet.data.JSONParser;
import dev.heartflame.fleet.model.s2c.S2CActionObject;
import dev.heartflame.fleet.model.s2c.S2CStressObject;
import dev.heartflame.fleet.monitor.SystemMonitorExecutor;
import dev.heartflame.fleet.packet.senders.BotActions;
import dev.heartflame.fleet.util.ActionType;
import dev.heartflame.fleet.util.HLogger;

import javax.net.ssl.SSLSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientThread extends Thread {

    private SSLSocket socket;
    private DataInputStream dIn;
    private DataOutputStream dOut;

    public ClientThread(SSLSocket socket) {
        try {
            this.socket = socket;

            this.dIn = new DataInputStream(socket.getInputStream());
            this.dOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException error) {
            error.printStackTrace();
            HLogger.error(error.getMessage());
        }
    }

    @Override
    public void run() {

        try {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            Runnable task = () -> {
                SystemMonitorExecutor.gatherData().thenAccept(stats -> {
                    try {

                        ObjectMapper mapper = new ObjectMapper();
                        String data = mapper.writeValueAsString(stats);

                        byte[] message = HCrypto.encrypt(data);

                        dOut.writeInt(message.length);
                        dOut.write(message);

                    } catch (IOException error) {
                        error.printStackTrace();
                    }
                });
            };

            scheduler.scheduleAtFixedRate(task, 1, JSONParser.fetchStatisticInterval(), TimeUnit.SECONDS);

            ObjectMapper mapper = new ObjectMapper();
            while (true) {
                try {
                    int length = dIn.readInt();

                    if (length > 0) {
                        byte[] message = new byte[length];
                        dIn.readFully(message, 0, message.length);

                        byte[] decrypted = HCrypto.decrypt(message);
                        String str = new String(decrypted, StandardCharsets.UTF_8);

                        JsonNode jsonNode = mapper.readTree(str);
                        String type = jsonNode.get("type").asText();
                        switch (type) {

                            case "action":
                                S2CActionObject actionObject = mapper.readValue(str, S2CActionObject.class);
                                BotActions.preparse(actionObject);
                                break;

                            case "init":
                                S2CStressObject stressObject = mapper.readValue(str, S2CStressObject.class);

                                if (BotHandler.ongoingStress != null) {
                                    HLogger.warn("Attempted to start another stress when one is already ongoing!");
                                    return;
                                }
                                BotHandler.initialiseBots(stressObject);
                                break;

                            case "cancel":
                                synchronized (BotHandler.class) {
                                    BotHandler.cancel = true;
                                    BotActions.disconnect("Stress cancelled.", ActionType.ALL, false);
                                    break;
                                }

                            case "modify_interval":
                                if (BotHandler.ongoingStress != null) {
                                    HLogger.debug(String.format("Received instructions to modify stress interval to [%d]", jsonNode.get("interval").asInt()));
                                    JSONParser.writePersistentInterval(jsonNode.get("interval").asInt());
                                }
                                break;

                            case "modify_bot_count":
                                if (BotHandler.ongoingStress != null) {
                                    HLogger.debug(String.format("Received instructions to modify bot count to [%d]", jsonNode.get("botCount").asInt()));
                                    if (BotHandler.completed) {
                                        BotHandler.continueLoop(jsonNode.get("botCount").asInt());
                                    } else {
                                        JSONParser.writePersistentBotCount(jsonNode.get("botCount").asInt());
                                        BotHandler.botCount = jsonNode.get("botCount").asInt();
                                    }

                                }
                                break;

                            case "cancel_tasks":
                                BotActions.cancel(str);
                                break;

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            HLogger.error("Error while reading from socket: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                dIn.close();
                dOut.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
