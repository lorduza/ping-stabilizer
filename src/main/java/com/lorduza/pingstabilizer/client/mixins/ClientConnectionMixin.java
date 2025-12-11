package com.lorduza.pingstabilizer.client.mixins;

import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.lib.TcpPriorityHandler;
import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Shadow private Channel channel;

    @Inject(method = "channelActive", at = @At("TAIL"))
    private void onChannelActive(io.netty.channel.ChannelHandlerContext ctx, CallbackInfo ci) {

        TcpPriorityHandler.apply(ctx.channel());
    }
}


