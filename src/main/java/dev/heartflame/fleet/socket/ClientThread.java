package dev.heartflame.fleet.socket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.heartflame.fleet.bot.BotHandler;
import dev.heartflame.fleet.crypto.HCrypto;
import dev.heartflame.fleet.data.JSONParser;
import dev.heartflame.fleet.model.c2s.C2SStatisticsObject;
import dev.heartflame.fleet.model.s2c.S2CActionObject;
import dev.heartflame.fleet.model.s2c.S2CStressObject;
import dev.heartflame.fleet.monitor.SystemMonitorExecutor;
import dev.heartflame.fleet.packet.senders.BotActions;
import dev.heartflame.fleet.util.HLogger;

import javax.net.ssl.SSLSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
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
            System.out.println(socket);

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
            /*ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> {

                try {
                    System.out.println("sending data");

                    C2SStatisticsObject statistics = SystemMonitorExecutor.gatherData();
                    System.out.println(statistics);
                    // todo grab bot data
                    ObjectMapper mapper = new ObjectMapper();
                    String data = mapper.writeValueAsString(statistics);
                    System.out.println(data);

                    byte[] message = HCrypto.encrypt(data);
                    System.out.println(message);

                    dOut.writeInt(message.length);
                    dOut.write(message);

                    System.out.println("sent");

                } catch (IOException e) {
                    HLogger.error("Error while sending node stats: " + e.getMessage());
                    e.printStackTrace();
                }

            }, 0, JSONParser.fetchStatisticInterval(), TimeUnit.SECONDS);*/
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            Runnable task = () -> {
                System.out.println("RUNNING");
                SystemMonitorExecutor.gatherData().thenAccept(stats -> {
                    try {
                        System.out.println("MAPPING");
                        ObjectMapper mapper = new ObjectMapper();
                        String data = mapper.writeValueAsString(stats);
                        System.out.println(data);

                        byte[] message = HCrypto.encrypt(data);

                        dOut.writeInt(message.length);
                        dOut.write(message);

                        System.out.println("SENT");
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
                                BotActions.parse(actionObject);
                                break;

                            case "init":
                                S2CStressObject stressObject = mapper.readValue(str, S2CStressObject.class);
                                BotHandler.initialiseBots(stressObject);
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
