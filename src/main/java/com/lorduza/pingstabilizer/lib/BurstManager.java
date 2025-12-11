package com.lorduza.pingstabilizer.lib;

import io.netty.channel.Channel;

public class BurstManager {
    
    private static volatile long lastBurstTime = 0;
    
    /**
     * Trigger burst flush - called when player attacks or does critical action
     * Bypasses SmartQueue and forces immediate transmission
     */
    public static void triggerBurst(Channel channel) {
        long now = System.currentTimeMillis();
        
        // Spam protection - max once per 50ms
        if (now - lastBurstTime < 50) return;
        
        lastBurstTime = now;
        
        if (channel != null && channel.isActive()) {
            // Flush SmartQueue
            SmartQueueManager.forceFlush();
            // Force channel flush
            channel.flush();
        }
    }
}
