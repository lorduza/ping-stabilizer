package com.lorduza.pingstabilizer.client.mixins;

import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.config.PingStabilizerConfig;
import com.lorduza.pingstabilizer.lib.NetworkStats;
import com.lorduza.pingstabilizer.lib.PacketClassifier;
import com.lorduza.pingstabilizer.lib.TcpPriorityHandler;
import com.lorduza.pingstabilizer.lib.SmartQueueManager;
import com.lorduza.pingstabilizer.lib.LatencySensor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

@Mixin(ClientConnection.class)
public class SendPacketMixin {
    @Shadow private Channel channel;

    @Inject(method = "sendImmediately", at = @At("HEAD"), cancellable = true)
    private void onSendHead(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        if (channel == null || !channel.isActive()) return;

        // 1. Verify Latency Sensor (Always track KeepAlive)
        if (packet instanceof KeepAliveC2SPacket) {
            LatencySensor.onKeepAliveSent();
            com.lorduza.pingstabilizer.lib.PingTracker.expectResponse();
        }

        // 2. Smart Queue Processing
        // If SmartQueue handles the packet (queues or flushes it), we cancel the vanilla send
        if (SmartQueueManager.processPacket(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "sendImmediately", at = @At("TAIL"))
    private void onSendTail(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        if (channel == null || !channel.isActive()) return;
        
        PingStabilizerConfig config = ConfigManager.get();
        
        NetworkStats.markSent();

        // Fallback Priority Flush (Only runs if SmartQueue is disabled or didn't handle the packet)
        if (config.priorityFlush) {
            PacketClassifier.Category category = PacketClassifier.classify(packet);
            if (category == PacketClassifier.Category.CRITICAL) {
                if (!flush) {
                    TcpPriorityHandler.forceFlush(channel);
                }
            }
        }
    }
}
