package com.lorduza.pingstabilizer.lib;

import com.lorduza.pingstabilizer.PingStabilizerMod;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Latency Sensor - Measures RTT and Jitter from Keep-Alive packets
 * 
 * Tracks round-trip time between client and server to detect
 * network quality changes in real-time.
 */
public class LatencySensor {
    
    private static final int SAMPLE_SIZE = 20;
    private static final Queue<Long> rttSamples = new LinkedList<>();
    
    private static volatile long lastKeepAliveSent = 0;
    private static volatile long currentRTT = 0;
    private static volatile long averageRTT = 0;
    private static volatile long jitter = 0;
    private static volatile long minRTT = Long.MAX_VALUE;
    private static volatile long maxRTT = 0;
    
    // Network quality thresholds
    private static final long GOOD_RTT = 50;
    private static final long MEDIUM_RTT = 100;
    private static final long HIGH_RTT = 150;
    
    public enum NetworkQuality {
        EXCELLENT,  // RTT < 50ms, jitter < 10ms
        GOOD,       // RTT < 100ms, jitter < 20ms
        MEDIUM,     // RTT < 150ms, jitter < 40ms
        POOR,       // RTT >= 150ms or jitter >= 40ms
        CRITICAL    // RTT >= 200ms or packet loss detected
    }
    
    /**
     * Called when a Keep-Alive packet is sent
     */
    public static void onKeepAliveSent() {
        lastKeepAliveSent = System.currentTimeMillis();
    }
    
    /**
     * Called when a Keep-Alive response is received
     */
    public static void onKeepAliveReceived() {
        if (lastKeepAliveSent > 0) {
            long rtt = System.currentTimeMillis() - lastKeepAliveSent;
            recordRTT(rtt);
            lastKeepAliveSent = 0;
        }
    }
    
    /**
     * Record an RTT sample from any source (ping packet, keep-alive, etc.)
     */
    public static void recordRTT(long rtt) {
        currentRTT = rtt;
        
        // Track min/max
        if (rtt < minRTT) minRTT = rtt;
        if (rtt > maxRTT) maxRTT = rtt;
        
        synchronized (rttSamples) {
            rttSamples.add(rtt);
            if (rttSamples.size() > SAMPLE_SIZE) {
                rttSamples.poll();
            }
            
            // Calculate average
            long sum = 0;
            for (Long sample : rttSamples) {
                sum += sample;
            }
            averageRTT = rttSamples.isEmpty() ? rtt : sum / rttSamples.size();
            
            // Calculate jitter (average deviation from mean)
            if (rttSamples.size() >= 2) {
                long deviation = 0;
                for (Long sample : rttSamples) {
                    deviation += Math.abs(sample - averageRTT);
                }
                jitter = deviation / rttSamples.size();
                // Check for spikes
                JitterStabilizer.check((int)jitter);
            }
        }
        
        PingStabilizerMod.LOGGER.debug("RTT: {}ms, Avg: {}ms, Jitter: {}ms, Quality: {}", 
            rtt, averageRTT, jitter, getNetworkQuality());
    }
    
    /**
     * Get current network quality assessment
     */
    public static NetworkQuality getNetworkQuality() {
        if (averageRTT >= 200 || jitter >= 60) {
            return NetworkQuality.CRITICAL;
        }
        if (averageRTT >= HIGH_RTT || jitter >= 40) {
            return NetworkQuality.POOR;
        }
        if (averageRTT >= MEDIUM_RTT || jitter >= 20) {
            return NetworkQuality.MEDIUM;
        }
        if (averageRTT >= GOOD_RTT || jitter >= 10) {
            return NetworkQuality.GOOD;
        }
        return NetworkQuality.EXCELLENT;
    }
    
    /**
     * Check if network is congested (should hold BULK packets)
     */
    public static boolean isNetworkCongested() {
        NetworkQuality quality = getNetworkQuality();
        return quality == NetworkQuality.POOR || quality == NetworkQuality.CRITICAL;
    }
    
    /**
     * Check if we're in a spike (current RTT much higher than average)
     */
    public static boolean isInSpike() {
        return currentRTT > (averageRTT * 1.5) && currentRTT > 100;
    }
    
    // Getters
    public static long getCurrentRTT() { return currentRTT; }
    public static long getAverageRTT() { return averageRTT; }
    public static long getJitter() { return jitter; }
    public static long getMinRTT() { return minRTT == Long.MAX_VALUE ? 0 : minRTT; }
    public static long getMaxRTT() { return maxRTT; }
    
    /**
     * Get quality color for HUD
     */
    public static int getQualityColor() {
        switch (getNetworkQuality()) {
            case EXCELLENT: return 0x00FF00; // Bright Green
            case GOOD: return 0xAAFF00;      // Yellow-Green
            case MEDIUM: return 0xFFFF00;    // Yellow
            case POOR: return 0xFFAA00;      // Orange
            case CRITICAL: return 0xFF0000;  // Red
            default: return 0xFFFFFF;
        }
    }
}
