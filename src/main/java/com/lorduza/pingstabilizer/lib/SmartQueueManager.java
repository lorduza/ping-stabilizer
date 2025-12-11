package com.lorduza.pingstabilizer.lib;

import com.lorduza.pingstabilizer.PingStabilizerMod;
import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.config.NetBoostConfig;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import net.minecraft.network.packet.Packet;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SmartQueueManager - Intelligent packet queue with timeout safety
 * 
 * Key principles:
 * 1. CRITICAL packets are NEVER queued - sent immediately
 * 2. BULK packets can be batched during congestion
 * 3. No packet is held longer than maxHoldMs (default: 100ms)
 * 4. Only affects C2S (outgoing) packets
 * 5. All channel operations run on Netty EventLoop (thread-safe)
 */
public class SmartQueueManager {
    
    private static final ConcurrentLinkedQueue<QueuedPacket> bulkQueue = new ConcurrentLinkedQueue<>();
    private static volatile Channel channel;
    private static long lastFlushTime = 0;
    private static final AtomicInteger queuedCount = new AtomicInteger(0);
    private static final AtomicInteger processedCount = new AtomicInteger(0);
    
    // Stats for HUD
    private static volatile int lastQueueSize = 0;
    private static volatile int packetsHeld = 0;
    
    /**
     * Queued packet with timestamp for timeout
     */
    private static class QueuedPacket {
        final Packet<?> packet;
        final long queueTime;
        
        QueuedPacket(Packet<?> packet) {
            this.packet = packet;
            this.queueTime = System.currentTimeMillis();
        }
        
        boolean isExpired(int maxHoldMs) {
            return System.currentTimeMillis() - queueTime > maxHoldMs;
        }
    }
    
    /**
     * Set the network channel
     */
    public static void setChannel(Channel ch) {
        channel = ch;
    }
    
    /**
     * Get the network channel
     */
    public static Channel getChannel() {
        return channel;
    }
    
    /**
     * Process an outgoing packet - THREAD SAFE
     * Uses EventLoop to ensure all operations happen on correct thread
     * @return true if packet was handled (queued or sent), false if should use default handling
     */
    public static boolean processPacket(Packet<?> packet) {
        NetBoostConfig config = ConfigManager.get();
        
        // If smart queue is disabled, don't handle
        if (!config.smartQueue) {
            return false;
        }
        
        Channel ch = channel;
        if (ch == null || !ch.isActive()) {
            return false;
        }
        
        PacketClassifier.Category category = PacketClassifier.classify(packet);
        
        // CRITICAL AND NORMAL PACKETS - NEVER QUEUE!
        // Queuing NORMAL packets causes "lag feel" even when ping is good
        if (category == PacketClassifier.Category.CRITICAL || category == PacketClassifier.Category.NORMAL) {
            sendOnEventLoop(ch, packet, true); // Always flush immediately
            processedCount.incrementAndGet();
            return true;
        }
        
        // ONLY BULK PACKETS CAN BE QUEUED
        // Chat, menu, stats - not gameplay critical
        if (category == PacketClassifier.Category.BULK) {
            // Jitter Stabilizer - Drop non-essential packets during lag spikes
            if (JitterStabilizer.isStabilizing()) {
                return true; // Dropped
            }
            
            if (config.adaptiveThrottle && LatencySensor.isNetworkCongested()) {
                queuePacket(packet); // Only queue if network is congested
                return true;
            }
            sendOnEventLoop(ch, packet, false); // No flush needed for bulk
            processedCount.incrementAndGet();
            return true;
        }
        
        return false;
    }
    
    /**
     * Queue a packet for delayed sending (thread-safe)
     */
    private static void queuePacket(Packet<?> packet) {
        bulkQueue.add(new QueuedPacket(packet));
        queuedCount.incrementAndGet();
        packetsHeld++;
    }
    
    /**
     * Send packet on EventLoop - THREAD SAFE
     * Ensures channel operations happen on correct Netty thread
     */
    private static void sendOnEventLoop(Channel ch, Packet<?> packet, boolean flush) {
        if (ch == null || !ch.isActive()) return;
        
        EventLoop eventLoop = ch.eventLoop();
        
        // If already on EventLoop, execute directly
        if (eventLoop.inEventLoop()) {
            if (flush) {
                ch.writeAndFlush(packet);
            } else {
                ch.write(packet);
            }
        } else {
            // Schedule on EventLoop
            eventLoop.execute(() -> {
                if (ch.isActive()) {
                    if (flush) {
                        ch.writeAndFlush(packet);
                    } else {
                        ch.write(packet);
                    }
                }
            });
        }
    }
    
    /**
     * Tick - called every game tick to flush queued packets
     * Ensures no packet is held longer than maxHoldMs
     * All channel operations are scheduled on EventLoop
     */
    public static void tick() {
        NetBoostConfig config = ConfigManager.get();
        Channel ch = channel;
        
        if (!config.smartQueue || ch == null || !ch.isActive()) {
            // Queue disabled or no channel - flush everything
            flushAllQueued(ch);
            return;
        }
        
        // Process queue on EventLoop
        EventLoop eventLoop = ch.eventLoop();
        final int maxHold = config.maxHoldMs;
        
        // Schedule queue processing on EventLoop
        if (eventLoop.inEventLoop()) {
            processQueueInternal(ch, maxHold);
        } else {
            eventLoop.execute(() -> processQueueInternal(ch, maxHold));
        }
    }
    
    /**
     * Internal queue processing - MUST run on EventLoop
     */
    private static void processQueueInternal(Channel ch, int maxHoldMs) {
        if (ch == null || !ch.isActive()) return;
        
        long now = System.currentTimeMillis();
        int flushed = 0;
        
        // Process queue - send expired packets and batch the rest
        while (!bulkQueue.isEmpty()) {
            QueuedPacket queued = bulkQueue.peek();
            if (queued == null) break;
            
            // Check timeout - packet held too long, MUST send now
            if (queued.isExpired(maxHoldMs)) {
                bulkQueue.poll();
                ch.write(queued.packet);
                processedCount.incrementAndGet();
                flushed++;
                continue;
            }
            
            // Network is no longer congested, flush everything
            if (!LatencySensor.isNetworkCongested()) {
                bulkQueue.poll();
                ch.write(queued.packet);
                processedCount.incrementAndGet();
                flushed++;
                continue;
            }
            
            // Still congested and not expired, leave in queue
            break;
        }
        
        // Flush if we sent anything
        if (flushed > 0) {
            ch.flush();
            lastFlushTime = now;
        }
        
        // Update stats
        lastQueueSize = bulkQueue.size();
    }
    
    /**
     * Flush all queued packets - used when queue is disabled or disconnecting
     */
    private static void flushAllQueued(Channel ch) {
        if (bulkQueue.isEmpty()) return;
        
        if (ch == null || !ch.isActive()) {
            // No channel, just clear the queue
            bulkQueue.clear();
            packetsHeld = 0;
            lastQueueSize = 0;
            return;
        }
        
        EventLoop eventLoop = ch.eventLoop();
        
        Runnable flushTask = () -> {
            while (!bulkQueue.isEmpty()) {
                QueuedPacket queued = bulkQueue.poll();
                if (queued != null && ch.isActive()) {
                    ch.write(queued.packet);
                    processedCount.incrementAndGet();
                }
            }
            if (ch.isActive()) {
                ch.flush();
            }
            packetsHeld = 0;
            lastQueueSize = 0;
        };
        
        if (eventLoop.inEventLoop()) {
            flushTask.run();
        } else {
            eventLoop.execute(flushTask);
        }
    }
    
    /**
     * Force flush all queued packets (e.g., on disconnect)
     */
    public static void forceFlush() {
        flushAllQueued(channel);
    }
    
    // === Stats for HUD ===
    
    public static int getQueueSize() {
        return lastQueueSize;
    }
    
    public static int getPacketsHeld() {
        return packetsHeld;
    }
    
    public static int getProcessedCount() {
        return processedCount.get();
    }
    
    public static void resetStats() {
        packetsHeld = 0;
        processedCount.set(0);
        queuedCount.set(0);
    }
}
