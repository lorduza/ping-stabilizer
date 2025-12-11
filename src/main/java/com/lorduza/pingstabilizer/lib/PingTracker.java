package com.lorduza.pingstabilizer.lib;

import java.util.LinkedList;
import java.util.Queue;


public class PingTracker {
    private static final int HISTORY_SIZE = 20;
    private static final Queue<Long> pingHistory = new LinkedList<>();
    private static volatile long lastPing = 0;
    private static volatile long avgPing = 0;
    private static volatile long jitter = 0;
    private static volatile int packetLoss = 0;

    private static volatile long expectedResponses = 0;
    private static volatile long receivedResponses = 0;
    private static volatile long lastResetTime = System.currentTimeMillis();
    
    
    private static long lastRecordTime = 0;

    public static void recordPing(long pingMs) {
        long now = System.currentTimeMillis();

        if (pingMs != lastPing || now - lastRecordTime > 1000) {
            lastPing = pingMs;
            lastRecordTime = now;

        
        synchronized (pingHistory) {
            pingHistory.add(pingMs);
            if (pingHistory.size() > HISTORY_SIZE) {
                pingHistory.poll();
            }

            long sum = 0;
            for (Long p : pingHistory) {
                sum += p;
            }
            avgPing = pingHistory.isEmpty() ? 0 : sum / pingHistory.size();

            if (pingHistory.size() >= 2) {
                long variance = 0;
                for (Long p : pingHistory) {
                    variance += Math.abs(p - avgPing);
                }
                jitter = variance / pingHistory.size();
            }
        }
        }
    }
    
    
    public static void expectResponse() {
        expectedResponses++;

        long now = System.currentTimeMillis();
        if (now - lastResetTime > 30000) {
            resetPacketLoss();
        }
    }
    
    
    public static void responseReceived() {
        receivedResponses++;
        updatePacketLoss();
    }
    
    private static void updatePacketLoss() {
        if (expectedResponses > 0) {
            long lost = expectedResponses - receivedResponses;
            packetLoss = (int) Math.max(0, Math.min(100, (lost * 100) / expectedResponses));
        }
    }
    
    private static void resetPacketLoss() {
        expectedResponses = 0;
        receivedResponses = 0;
        packetLoss = 0;
        lastResetTime = System.currentTimeMillis();
    }
    
    public static long getLastPing() {
        return lastPing;
    }
    
    public static long getAvgPing() {
        return avgPing;
    }
    
    public static long getJitter() {
        return jitter;
    }
    
    public static int getPacketLoss() {
        return packetLoss;
    }
}


