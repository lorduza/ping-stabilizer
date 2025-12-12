package com.lorduza.pingstabilizer.client.hud;

import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.config.PingStabilizerConfig;
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
        PingStabilizerConfig config = ConfigManager.get();
        net.minecraft.client.font.TextRenderer tr = net.minecraft.client.MinecraftClient.getInstance().textRenderer;
        net.minecraft.text.Text text;


        if (!config.hudEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();


        if (client.getDebugHud().shouldShowDebugHud()) return;

        if (client.options.hudHidden) return;
        
        if (client.player == null) return;

        long displayPing = getPing(client);

        long displayJitter = LatencySensor.getJitter();




        if (config.showJitter) {
            String val = displayJitter + "ms";
            if (JitterStabilizer.isStabilizing()) {
                val += " " + net.minecraft.text.Text.translatable("hud.pingstabilizer.status.stabilizing").getString();
            }
            text = net.minecraft.text.Text.translatable("hud.pingstabilizer.label.jitter", val);
            drawContext.drawTextWithShadow(tr, text, config.jitterX, config.jitterY, 
                JitterStabilizer.isStabilizing() ? 0xFFFF5555 : getJitterColor(displayJitter));
        }

        if (config.showPacketLoss) {
            text = net.minecraft.text.Text.translatable("hud.pingstabilizer.label.loss", PingTracker.getPacketLoss() + "%");
            drawContext.drawTextWithShadow(tr, text, config.lossX, config.lossY, 0xFF55FF55);
        }

        if (config.showNetworkQuality) {
            String qualityKey = "hud.pingstabilizer.quality.excellent";
            int color = 0xFF55FF55;
            if (displayPing > 150) { qualityKey = "hud.pingstabilizer.quality.poor"; color = 0xFFFF5555; }
            else if (displayPing > 100) { qualityKey = "hud.pingstabilizer.quality.fair"; color = 0xFFFFAA00; }
            else if (displayPing > 50) { qualityKey = "hud.pingstabilizer.quality.good"; color = 0xFFFFFF55; }
            text = net.minecraft.text.Text.translatable("hud.pingstabilizer.label.quality", net.minecraft.text.Text.translatable(qualityKey));
            drawContext.drawTextWithShadow(tr, text, config.qualityX, config.qualityY, color);
        }

        if (config.showPacketStats) {
            int pps = NetworkStats.getPPS();
            text = net.minecraft.text.Text.translatable("hud.pingstabilizer.label.pps", pps);
            drawContext.drawTextWithShadow(tr, text, config.ppsX, config.ppsY, 0xFFCCCCCC);
        }


    }
    
    private long getPing(MinecraftClient client) {
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler == null || client.player == null) return -1;

        PlayerListEntry entry = handler.getPlayerListEntry(client.player.getUuid());
        if (entry != null) {
            int latency = entry.getLatency();
            if (latency > 0) return latency;
        }

        return -1;
    }
    

    
    private int getJitterColor(long jitter) {
        if (jitter < 15) return 0xFF55FF55; // Green
        if (jitter < 30) return 0xFFFFFF55; // Yellow
        return 0xFFFF5555; // Red
    }
}


