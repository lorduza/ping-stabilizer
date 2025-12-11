package com.lorduza.pingstabilizer.client.mixins;

import com.lorduza.pingstabilizer.lib.LatencySensor;
import com.lorduza.pingstabilizer.lib.PingTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class KeepAliveMixin {
    
    @Inject(method = "onKeepAlive", at = @At("HEAD"))
    private void onKeepAliveReceived(KeepAliveS2CPacket packet, CallbackInfo ci) {

        LatencySensor.onKeepAliveReceived();

        PingTracker.responseReceived();
    }
}


