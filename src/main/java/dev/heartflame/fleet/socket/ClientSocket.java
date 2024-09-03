package dev.heartflame.fleet.socket;

import dev.heartflame.fleet.data.JSONParser;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class ClientSocket extends Thread {

    @Override
    public void run() {

        System.setProperty("javax.net.debug", "all");


        try {
            String keystorePath = JSONParser.fetchKeystorePath();
            String keystorePassword = JSONParser.fetchKeystorePassword();
            String truststorePath = JSONParser.fetchTruststorePath();
            String truststorePassword = JSONParser.fetchTruststorePassword();

            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(keystorePath)) {
                ks.load(fis, keystorePassword.toCharArray());
            }

            KeyStore ts = KeyStore.getInstance("PKCS12");
            try (FileInputStream tst = new FileInputStream(truststorePath)) {
                ts.load(tst, truststorePassword.toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keystorePassword.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            String endpoint = JSONParser.fetchEndpoint();
            int port = JSONParser.fetchSocketPort();

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(endpoint, port);

            sslSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

            ClientThread clientThread = new ClientThread(sslSocket);
            clientThread.start();
        } catch (Exception e) {
            System.err.println("Error initializing SSL connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
