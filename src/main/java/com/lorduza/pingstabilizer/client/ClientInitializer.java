package com.lorduza.pingstabilizer.client;

import com.lorduza.pingstabilizer.PingStabilizerMod;
import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.hud.NetworkHudRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.api.ClientModInitializer;

public class ClientInitializer implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        PingStabilizerMod.LOGGER.info("Ping Stabilizer Client Initializing...");
        ConfigManager.load();
        
        KeybindManager.register();
        HudRenderCallback.EVENT.register(new NetworkHudRenderer());
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            KeybindManager.tick(client);
        });
        
        PingStabilizerMod.LOGGER.info("Ping Stabilizer Client initialized!");
        PingStabilizerMod.LOGGER.info("Press H to edit HUD position, N to toggle HUD");
    }
}
