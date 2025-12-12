package com.lorduza.pingstabilizer.lib;

import com.lorduza.pingstabilizer.PingStabilizerMod;
import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.config.PingStabilizerConfig;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import net.minecraft.network.packet.Packet;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class SmartQueueManager {
    
    private static final ConcurrentLinkedQueue<QueuedPacket> bulkQueue = new ConcurrentLinkedQueue<>();
    private static volatile Channel channel;
    private static long lastFlushTime = 0;
    private static final AtomicInteger queuedCount = new AtomicInteger(0);
    private static final AtomicInteger processedCount = new AtomicInteger(0);

    private static volatile int lastQueueSize = 0;
    private static volatile int packetsHeld = 0;
    
    
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
    
    
    public static void setChannel(Channel ch) {
        channel = ch;
    }
    
    
    public static Channel getChannel() {
        return channel;
    }
    
    
    public static boolean processPacket(Packet<?> packet) {
        PingStabilizerConfig config = ConfigManager.get();

        if (!config.smartQueue) {
            return false;
        }
        
        Channel ch = channel;
        if (ch == null || !ch.isActive()) {
            return false;
        }
        
        PacketClassifier.Category category = PacketClassifier.classify(packet);


        if (category == PacketClassifier.Category.CRITICAL || category == PacketClassifier.Category.NORMAL) {
            sendOnEventLoop(ch, packet, true); // Always flush immediately
            processedCount.incrementAndGet();
            return true;
        }


        if (category == PacketClassifier.Category.BULK) {

            if (JitterStabilizer.isStabilizing()) {
                sendOnEventLoop(ch, packet, true); // Force flush if stabilizing
                processedCount.incrementAndGet();
                return true; 
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
    
    
    private static void queuePacket(Packet<?> packet) {
        bulkQueue.add(new QueuedPacket(packet));
        queuedCount.incrementAndGet();
        packetsHeld++;
    }
    
    
    private static void sendOnEventLoop(Channel ch, Packet<?> packet, boolean flush) {
        if (ch == null || !ch.isActive()) return;
        
        EventLoop eventLoop = ch.eventLoop();

        if (eventLoop.inEventLoop()) {
            if (flush) {
                ch.writeAndFlush(packet);
            } else {
                ch.write(packet);
            }
        } else {

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
    
    
    public static void tick() {
        PingStabilizerConfig config = ConfigManager.get();
        Channel ch = channel;
        
        if (!config.smartQueue || ch == null || !ch.isActive()) {

            flushAllQueued(ch);
            return;
        }

        EventLoop eventLoop = ch.eventLoop();
        final int maxHold = config.maxHoldMs;

        if (eventLoop.inEventLoop()) {
            processQueueInternal(ch, maxHold);
        } else {
            eventLoop.execute(() -> processQueueInternal(ch, maxHold));
        }
    }
    
    
    private static void processQueueInternal(Channel ch, int maxHoldMs) {
        if (ch == null || !ch.isActive()) return;
        
        long now = System.currentTimeMillis();
        int flushed = 0;

        while (!bulkQueue.isEmpty()) {
            QueuedPacket queued = bulkQueue.peek();
            if (queued == null) break;

            if (queued.isExpired(maxHoldMs)) {
                bulkQueue.poll();
                ch.write(queued.packet);
                processedCount.incrementAndGet();
                flushed++;
                continue;
            }

            if (!LatencySensor.isNetworkCongested()) {
                bulkQueue.poll();
                ch.write(queued.packet);
                processedCount.incrementAndGet();
                flushed++;
                continue;
            }

            break;
        }

        if (flushed > 0) {
            ch.flush();
            lastFlushTime = now;
        }

        lastQueueSize = bulkQueue.size();
    }
    
    
    private static void flushAllQueued(Channel ch) {
        if (bulkQueue.isEmpty()) return;
        
        if (ch == null || !ch.isActive()) {

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
    
    
    public static void forceFlush() {
        flushAllQueued(channel);
    }

    
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


