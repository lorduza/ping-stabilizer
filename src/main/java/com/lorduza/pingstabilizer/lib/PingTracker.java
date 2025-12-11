package com.lorduza.pingstabilizer.lib;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Tracks ping, jitter, and packet loss statistics.
 */
public class PingTracker {
    private static final int HISTORY_SIZE = 20;
    private static final Queue<Long> pingHistory = new LinkedList<>();
    private static volatile long lastPing = 0;
    private static volatile long avgPing = 0;
    private static volatile long jitter = 0;
    private static volatile int packetLoss = 0;
    
    // For packet loss calculation
    private static volatile long expectedResponses = 0;
    private static volatile long receivedResponses = 0;
    private static volatile long lastResetTime = System.currentTimeMillis();
    
    /**
     * Record a ping sample
     */
    public static void recordPing(long pingMs) {
        lastPing = pingMs;
        
        synchronized (pingHistory) {
            pingHistory.add(pingMs);
            if (pingHistory.size() > HISTORY_SIZE) {
                pingHistory.poll();
            }
            
            // Calculate average
            long sum = 0;
            for (Long p : pingHistory) {
                sum += p;
            }
            avgPing = pingHistory.isEmpty() ? 0 : sum / pingHistory.size();
            
            // Calculate jitter (variance from average)
            if (pingHistory.size() >= 2) {
                long variance = 0;
                for (Long p : pingHistory) {
                    variance += Math.abs(p - avgPing);
                }
                jitter = variance / pingHistory.size();
            }
        }
    }
    
    /**
     * Mark that we expect a response (sent a request)
     */
    public static void expectResponse() {
        expectedResponses++;
        
        // Reset counters every 30 seconds
        long now = System.currentTimeMillis();
        if (now - lastResetTime > 30000) {
            resetPacketLoss();
        }
    }
    
    /**
     * Mark that we received a response
     */
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
