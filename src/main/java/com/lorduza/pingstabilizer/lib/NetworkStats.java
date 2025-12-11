package com.lorduza.pingstabilizer.lib;

import java.util.concurrent.atomic.LongAdder;

public class NetworkStats {
    // Total packets (for internal tracking if needed)
    private static final LongAdder totalPackets = new LongAdder();
    
    // PPS Tracking
    private static final LongAdder packetsThisSecond = new LongAdder();
    private static volatile int ppsCallback = 0;
    private static volatile long lastPPSTime = System.currentTimeMillis();
    
    public static void markSent() {
        totalPackets.increment();
        packetsThisSecond.increment();
        
        long now = System.currentTimeMillis();
        // Update PPS every second
        if (now - lastPPSTime >= 1000) {
            ppsCallback = packetsThisSecond.intValue();
            packetsThisSecond.reset();
            lastPPSTime = now;
        }
    }

    
    // Get total packets sent since game start
    public static long getPacketsSent() {
        return totalPackets.sum();
    }
    
    // Get Packets Per Second
    public static int getPPS() {
        // If more than 1.5 seconds have passed without update (idle), return 0
        if (System.currentTimeMillis() - lastPPSTime > 1500) {
            return 0; // Return 0 if idle
        }
        
        // Return current accumulating count for instant feedback if > last stored value
        // meaningful particularly at start of new second
        int current = packetsThisSecond.intValue();
        return Math.max(ppsCallback, current);
    }
}
