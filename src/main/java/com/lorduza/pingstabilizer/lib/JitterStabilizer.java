package com.lorduza.pingstabilizer.lib;

import com.lorduza.pingstabilizer.PingStabilizerMod;
import io.netty.channel.Channel;

/**
 * JitterStabilizer - Kills lag spikes before they kill you.
 * 
 * Logic:
 * 1. Monitors Ping Variance (Jitter).
 * 2. If Jitter > threshold (e.g. 30ms), it activates "STABILIZE MODE".
 * 3. In Stabilize Mode, it deprioritizes/drops BULK packets to clear the line.
 * 4. Once stable, it resumes normal operation.
 */
public class JitterStabilizer {
    
    private static boolean stabilizing = false;
    private static long lastSpikeTime = 0;
    
    // Configurable thresholds
    private static final int JITTER_THRESHOLD_MS = 30;
    private static final int STABILIZE_DURATION_MS = 2000; // Stay in stabilize mode for 2s after spike
    
    public static void check(int currentJitter) {
        if (currentJitter > JITTER_THRESHOLD_MS) {
            triggerStabilization(currentJitter);
        } else if (stabilizing && System.currentTimeMillis() - lastSpikeTime > STABILIZE_DURATION_MS) {
            endStabilization();
        }
    }
    
    private static void triggerStabilization(int jitter) {
        if (!stabilizing) {
            stabilizing = true;
            PingStabilizerMod.LOGGER.warn("⚠️ LAG SPIKE DETECTED (Jitter: " + jitter + "ms) - Stabilizing...");
            
            // EMERGENCY FLUSH: Clear the channel immediately
            Channel ch = SmartQueueManager.getChannel();
            if (ch != null && ch.isActive()) {
                ch.flush();
            }
        }
        lastSpikeTime = System.currentTimeMillis();
    }
    
    private static void endStabilization() {
        stabilizing = false;
        PingStabilizerMod.LOGGER.info("✅ Connection Stabilized.");
    }
    
    public static boolean isStabilizing() {
        return stabilizing;
    }
}
