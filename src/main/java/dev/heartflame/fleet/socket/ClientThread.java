package dev.heartflame.fleet.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.heartflame.fleet.crypto.HCrypto;
import dev.heartflame.fleet.data.JSONParser;
import dev.heartflame.fleet.model.internal.HardwareStatisticsModel;
import dev.heartflame.fleet.monitor.SystemMonitorExecutor;

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

    public ClientThread(SSLSocket socket) throws IOException {
        this.socket = socket;

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> {

                try {
                    System.out.println("sending data");

                    HardwareStatisticsModel statistics = SystemMonitorExecutor.gatherData();
                    ObjectMapper mapper = new ObjectMapper();
                    String data = mapper.writeValueAsString(statistics);

                    byte[] message = HCrypto.encrypt(data);

                    dOut.writeInt(message.length);
                    dOut.write(message);

                    System.out.println("sent");

                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                }

            }, 0, JSONParser.fetchStatisticInterval(), TimeUnit.SECONDS);

    }

    @Override
    public void run() {

        try {
            dIn = new DataInputStream(socket.getInputStream());
            dOut = new DataOutputStream(socket.getOutputStream());

            while (true) {
                try {
                    int length = dIn.readInt();

                    if (length > 0) {
                        byte[] message = new byte[length];
                        dIn.readFully(message, 0, message.length);

                        byte[] decrypted = HCrypto.decrypt(message);
                        System.out.println(decrypted);
                        System.out.println(new String(decrypted, StandardCharsets.UTF_8));
                    }
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
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
