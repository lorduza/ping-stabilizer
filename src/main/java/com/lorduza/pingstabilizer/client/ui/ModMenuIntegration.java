package com.lorduza.pingstabilizer.client.ui;

import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.config.NetBoostConfig;
import com.lorduza.pingstabilizer.lib.DebugLogger;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("config.netbooster.title"));

            builder.setSavingRunnable(ConfigManager::save);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            NetBoostConfig config = ConfigManager.get();

            // Network Optimization
            ConfigCategory network = builder.getOrCreateCategory(Text.translatable("config.netbooster.category.network"));

            network.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.tcpNoDelay"), config.tcpNoDelay)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.tcpNoDelay"))
                    .setSaveConsumer(newValue -> config.tcpNoDelay = newValue)
                    .build());

            network.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.priorityFlush"), config.priorityFlush)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.priorityFlush"))
                    .setSaveConsumer(newValue -> config.priorityFlush = newValue)
                    .build());

            // Advanced Settings
            ConfigCategory advanced = builder.getOrCreateCategory(Text.translatable("config.netbooster.category.advanced"));

            advanced.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.customBufferSize"), config.customBufferSize)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.customBufferSize"))
                    .setSaveConsumer(newValue -> config.customBufferSize = newValue)
                    .build());

            advanced.addEntry(entryBuilder.startIntField(Text.translatable("config.netbooster.option.sendBufferKB"), config.sendBufferKB)
                    .setDefaultValue(128)
                    .setMin(16).setMax(1024)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.sendBufferKB"))
                    .setSaveConsumer(newValue -> config.sendBufferKB = newValue)
                    .build());

            advanced.addEntry(entryBuilder.startIntField(Text.translatable("config.netbooster.option.receiveBufferKB"), config.receiveBufferKB)
                    .setDefaultValue(128)
                    .setMin(16).setMax(1024)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.receiveBufferKB"))
                    .setSaveConsumer(newValue -> config.receiveBufferKB = newValue)
                    .build());
                    
            advanced.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.disableCompression"), config.disableCompression)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.disableCompression"))
                    .setSaveConsumer(newValue -> config.disableCompression = newValue)
                    .build());

            // Experimental
            ConfigCategory experimental = builder.getOrCreateCategory(Text.translatable("config.netbooster.category.experimental"));

            experimental.setDescription(new Text[]{
                Text.translatable("config.netbooster.experimental.warning")
            });

            experimental.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.smartQueue"), config.smartQueue)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.smartQueue"))
                    .setSaveConsumer(newValue -> config.smartQueue = newValue)
                    .build());

            experimental.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.adaptiveThrottle"), config.adaptiveThrottle)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.adaptiveThrottle"))
                    .setSaveConsumer(newValue -> config.adaptiveThrottle = newValue)
                    .build());

            experimental.addEntry(entryBuilder.startIntField(Text.translatable("config.netbooster.option.maxHoldMs"), config.maxHoldMs)
                    .setDefaultValue(100)
                    .setMin(50).setMax(500)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.maxHoldMs"))
                    .setSaveConsumer(newValue -> config.maxHoldMs = newValue)
                    .build());

            experimental.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.enableDebugLog"), config.enableDebugLog)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.enableDebugLog"))
                    .setSaveConsumer(newValue -> {
                        config.enableDebugLog = newValue;
                        if (newValue) {
                            DebugLogger.enable();
                        } else {
                            DebugLogger.disable();
                        }
                    })
                    .build());

            // HUD Settings
            ConfigCategory hud = builder.getOrCreateCategory(Text.translatable("config.netbooster.category.hud"));

            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.enableHud"), config.hudEnabled)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.enableHud"))
                    .setSaveConsumer(newValue -> config.hudEnabled = newValue)
                    .build());

            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.showPing"), config.showPing)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.showPing"))
                    .setSaveConsumer(newValue -> config.showPing = newValue)
                    .build());

            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.showJitter"), config.showJitter)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.showJitter"))
                    .setSaveConsumer(newValue -> config.showJitter = newValue)
                    .build());

            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.showPacketLoss"), config.showPacketLoss)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.showPacketLoss"))
                    .setSaveConsumer(newValue -> config.showPacketLoss = newValue)
                    .build());

            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.showPacketStats"), config.showPacketStats)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.showPacketStats"))
                    .setSaveConsumer(newValue -> config.showPacketStats = newValue)
                    .build());
            
            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netbooster.option.showNetworkQuality"), config.showNetworkQuality)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.netbooster.tooltip.showNetworkQuality"))
                    .setSaveConsumer(newValue -> config.showNetworkQuality = newValue)
                    .build());

            // Open Position Screen Button
            hud.addEntry(entryBuilder.startTextDescription(Text.translatable("config.netbooster.option.openHudPositioner"))
                    .setTooltip(Text.translatable("config.netbooster.tooltip.openHudPositioner"))
                    .build());

            return builder.build();
        };
    }
}
