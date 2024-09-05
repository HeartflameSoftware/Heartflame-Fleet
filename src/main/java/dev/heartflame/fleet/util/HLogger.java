package dev.heartflame.fleet.util;

public class HLogger {

    public static void info(String log) {
        synchronized (HLogger.class) {
            System.out.println("\u001B[34m" + log + "\u001B[0m");
        }
    }

    public static void error(String log) {
        synchronized (HLogger.class) {
            System.out.println("\u001B[31m" + log + "\u001B[0m");
        }
    }

    public static void debug(String log) {
        synchronized (HLogger.class) {
            System.out.println("\u001B[36m" + log + "\u001B[0m");
        }
    }

    public static void warn(String log) {
        synchronized (HLogger.class) {
            System.out.printf("\u001B[33m" + log + "\u001B[0m");
        }
    }

}
