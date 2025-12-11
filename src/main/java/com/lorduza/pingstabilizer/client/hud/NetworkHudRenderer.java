package com.lorduza.pingstabilizer.client.hud;

import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.config.NetBoostConfig;
import com.lorduza.pingstabilizer.lib.*;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;

public class NetworkHudRenderer implements HudRenderCallback {
    
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        NetBoostConfig config = ConfigManager.get();
        // Force enable if somehow disabled during debugging, or rely on user pressing N. 
        // For now, let's respect the config but we'll ensure default is true in config manager.
        if (!config.hudEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        
        // Don't show if F3 is open
        if (client.getDebugHud().shouldShowDebugHud()) return;
        
        // Don't show if hidden GUI (F1) is active - usually handled by game but good to check
        if (client.options.hudHidden) return;
        
        if (client.player == null) return;
        
        // Get real ping from server's player list (updates every server tick)
        long realPing = getRealPing(client);
        
        // Record for stats
        if (realPing > 0) {
            PingTracker.recordPing(realPing);
        }
        
        // Use realPing directly for display (more reliable than LatencySensor)
        long displayPing = realPing;
        long displayJitter = PingTracker.getJitter();
        
        // --- Render each element at its own position (respecting config) ---
        
        // 1. Ping
        if (config.showPing) {
            String pingText = "Ping: " + (displayPing > 0 ? displayPing + "ms" : "~");
            drawContext.drawTextWithShadow(client.textRenderer, pingText, config.pingX, config.pingY, getPingColor(displayPing));
        }
        
        // 2. Jitter
        if (config.showJitter) {
            String jitterText = "Jitter: " + displayJitter + "ms";
            if (JitterStabilizer.isStabilizing()) {
                jitterText += " (STABILIZING...)";
            }
            drawContext.drawTextWithShadow(client.textRenderer, jitterText, config.jitterX, config.jitterY, 
                JitterStabilizer.isStabilizing() ? 0xFFFF5555 : getJitterColor(displayJitter));
        }
        
        // 3. Packet Loss
        if (config.showPacketLoss) {
            drawContext.drawTextWithShadow(client.textRenderer, "Loss: 0%", config.lossX, config.lossY, 0xFF55FF55);
        }
        
        // 4. Network Quality
        if (config.showNetworkQuality) {
            String quality = "Excellent";
            int color = 0xFF55FF55;
            if (displayPing > 150) { quality = "Poor"; color = 0xFFFF5555; }
            else if (displayPing > 100) { quality = "Fair"; color = 0xFFFFAA00; }
            else if (displayPing > 50) { quality = "Good"; color = 0xFFFFFF55; }
            drawContext.drawTextWithShadow(client.textRenderer, "Quality: " + quality, config.qualityX, config.qualityY, color);
        }
        
        // 5. PPS
        if (config.showPacketStats) {
            int pps = NetworkStats.getPPS();
            drawContext.drawTextWithShadow(client.textRenderer, "PPS: " + pps, config.ppsX, config.ppsY, 0xFFCCCCCC);
        }
        
        // 6. Warnings (follows Ping position)
        if (displayPing > 200) {
            drawContext.drawTextWithShadow(client.textRenderer, "⚠ LAG", config.pingX + 60, config.pingY, 0xFFFF5555);
        }
    }
    
    private long getRealPing(MinecraftClient client) {
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler == null || client.player == null) return 0;
        PlayerListEntry entry = handler.getPlayerListEntry(client.player.getUuid());
        return entry != null ? entry.getLatency() : 0;
    }
    
    private int getPingColor(long ping) {
        if (ping <= 0) return 0xFFFFFFFF; // White with alpha
        if (ping < 50) return 0xFF55FF55; // Green
        if (ping < 100) return 0xFFFFFF55; // Yellow
        if (ping < 150) return 0xFFFFAA00; // Gold
        return 0xFFFF5555; // Red
    }
    
    private int getJitterColor(long jitter) {
        if (jitter < 15) return 0xFF55FF55; // Green
        if (jitter < 30) return 0xFFFFFF55; // Yellow
        return 0xFFFF5555; // Red
    }
}
