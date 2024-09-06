package dev.heartflame.fleet;

import dev.heartflame.fleet.data.JSONParser;
import dev.heartflame.fleet.monitor.SystemMonitorExecutor;
import dev.heartflame.fleet.socket.ClientSocket;

public class NodeMain {

    public static void main(String[] args) {
        // Initialise the socket
        new ClientSocket().run();

        // Initialise the system monitor.
        SystemMonitorExecutor.init(JSONParser.fetchStatisticInterval());
    }
}