package com.lorduza.pingstabilizer.client.mixins;

import com.lorduza.pingstabilizer.lib.BurstManager;
import com.lorduza.pingstabilizer.lib.SmartQueueManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class AttackMixin {
    
    @Shadow
    public ClientPlayerEntity player;
    
    @Inject(method = "doAttack", at = @At("HEAD"))
    private void onAttack(CallbackInfoReturnable<Boolean> cir) {
        if (player != null && player.networkHandler != null) {
            BurstManager.triggerBurst(SmartQueueManager.getChannel());
        }
    }
}


