package dev.heartflame.fleet.util;

public enum ActionType {

    ALL,
    RANDOM,
    SINGLE;

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) { this.username = username; }
}
