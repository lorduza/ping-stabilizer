package com.lorduza.pingstabilizer.lib;

public class KeepAliveManager {
    private static int intervalMs = 5000;

    public static int getIntervalMs() {
        return intervalMs;
    }

    public static void setIntervalMs(int ms) {
        intervalMs = Math.max(1000, ms);
    }
}


