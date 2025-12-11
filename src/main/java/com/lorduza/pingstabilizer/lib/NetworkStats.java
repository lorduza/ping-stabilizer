package com.lorduza.pingstabilizer.lib;

import java.util.concurrent.atomic.LongAdder;

public class NetworkStats {

    private static final LongAdder totalPackets = new LongAdder();

    private static final LongAdder packetsThisSecond = new LongAdder();
    private static volatile int ppsCallback = 0;
    private static volatile long lastPPSTime = System.currentTimeMillis();
    
    public static void markSent() {
        totalPackets.increment();
        packetsThisSecond.increment();
        
        long now = System.currentTimeMillis();

        if (now - lastPPSTime >= 1000) {
            ppsCallback = packetsThisSecond.intValue();
            packetsThisSecond.reset();
            lastPPSTime = now;
        }
    }

    public static long getPacketsSent() {
        return totalPackets.sum();
    }

    public static int getPPS() {

        if (System.currentTimeMillis() - lastPPSTime > 1500) {
            return 0; // Return 0 if idle
        }


        int current = packetsThisSecond.intValue();
        return Math.max(ppsCallback, current);
    }
}


