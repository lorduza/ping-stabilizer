package com.lorduza.pingstabilizer.lib;

import io.netty.channel.Channel;

public class BurstManager {
    
    private static volatile long lastBurstTime = 0;
    
    
    public static void triggerBurst(Channel channel) {
        long now = System.currentTimeMillis();

        if (now - lastBurstTime < 50) return;
        
        lastBurstTime = now;
        
        if (channel != null && channel.isActive()) {

            SmartQueueManager.forceFlush();

            channel.flush();
        }
    }
}


