package dev.heartflame.fleet;

import dev.heartflame.fleet.data.JSONParser;
import dev.heartflame.fleet.monitor.SystemMonitorExecutor;
import dev.heartflame.fleet.socket.ClientSocket;
import dev.heartflame.fleet.util.HLogger;

import java.util.Scanner;


public class NodeMain {

    public static void main(String[] args) {
        // Initialise the socket
        new ClientSocket().run();

        // Initialise the system monitor.
        SystemMonitorExecutor.init(JSONParser.fetchStatisticInterval());
    }

    public static void printNumberRange(int baseNumber, int secondaryNumber) {
        int start = (secondaryNumber - 1) * baseNumber + 1;
        int end = secondaryNumber * baseNumber;

        System.out.println("Outputting numbers from " + start + " to " + end);

        for (int i = start; i <= end; i++) {
            System.out.println(i);
        }
    }
}