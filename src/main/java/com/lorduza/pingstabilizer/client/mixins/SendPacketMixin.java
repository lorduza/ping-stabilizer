package com.lorduza.pingstabilizer.client.mixins;

import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.config.PingStabilizerConfig;
import com.lorduza.pingstabilizer.lib.NetworkStats;
import com.lorduza.pingstabilizer.lib.PacketClassifier;
import com.lorduza.pingstabilizer.lib.TcpPriorityHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

@Mixin(ClientConnection.class)
public class SendPacketMixin {
    @Shadow private Channel channel;

    @Inject(method = "sendImmediately", at = @At("TAIL"))
    private void onSendImmediately(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        if (channel == null || !channel.isActive()) return;
        
        PingStabilizerConfig config = ConfigManager.get();
        
        NetworkStats.markSent();
        
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


