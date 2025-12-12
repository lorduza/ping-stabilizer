package com.lorduza.pingstabilizer.client.ui;

import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.config.PingStabilizerConfig;
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
                    .setTitle(Text.translatable("config.pingstabilizer.title"));

            builder.setSavingRunnable(ConfigManager::save);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            PingStabilizerConfig config = ConfigManager.get();

            ConfigCategory network = builder.getOrCreateCategory(Text.translatable("config.pingstabilizer.category.network"));

            network.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pingstabilizer.option.tcpNoDelay"), config.tcpNoDelay)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.tcpNoDelay"))
                    .setSaveConsumer(newValue -> config.tcpNoDelay = newValue)
                    .build());

            network.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pingstabilizer.option.priorityFlush"), config.priorityFlush)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.priorityFlush"))
                    .setSaveConsumer(newValue -> config.priorityFlush = newValue)
                    .build());

            ConfigCategory advanced = builder.getOrCreateCategory(Text.translatable("config.pingstabilizer.category.advanced"));

            advanced.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pingstabilizer.option.customBufferSize"), config.customBufferSize)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.customBufferSize"))
                    .setSaveConsumer(newValue -> config.customBufferSize = newValue)
                    .build());

            advanced.addEntry(entryBuilder.startIntField(Text.translatable("config.pingstabilizer.option.sendBufferKB"), config.sendBufferKB)
                    .setDefaultValue(128)
                    .setMin(16).setMax(1024)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.sendBufferKB"))
                    .setSaveConsumer(newValue -> config.sendBufferKB = newValue)
                    .build());

            advanced.addEntry(entryBuilder.startIntField(Text.translatable("config.pingstabilizer.option.receiveBufferKB"), config.receiveBufferKB)
                    .setDefaultValue(128)
                    .setMin(16).setMax(1024)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.receiveBufferKB"))
                    .setSaveConsumer(newValue -> config.receiveBufferKB = newValue)
                    .build());
                    


            ConfigCategory smartQueue = builder.getOrCreateCategory(Text.translatable("config.pingstabilizer.category.smartQueue"));

            smartQueue.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pingstabilizer.option.smartQueue"), config.smartQueue)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.smartQueue"))
                    .setSaveConsumer(newValue -> config.smartQueue = newValue)
                    .build());

            smartQueue.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pingstabilizer.option.adaptiveThrottle"), config.adaptiveThrottle)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.adaptiveThrottle"))
                    .setSaveConsumer(newValue -> config.adaptiveThrottle = newValue)
                    .build());

            smartQueue.addEntry(entryBuilder.startIntField(Text.translatable("config.pingstabilizer.option.maxHoldMs"), config.maxHoldMs)
                    .setDefaultValue(100)
                    .setMin(50).setMax(500)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.maxHoldMs"))
                    .setSaveConsumer(newValue -> config.maxHoldMs = newValue)
                    .build());



            ConfigCategory hud = builder.getOrCreateCategory(Text.translatable("config.pingstabilizer.category.hud"));

            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pingstabilizer.option.enableHud"), config.hudEnabled)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.enableHud"))
                    .setSaveConsumer(newValue -> config.hudEnabled = newValue)
                    .build());

            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pingstabilizer.option.showPing"), config.showPing)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.showPing"))
                    .setSaveConsumer(newValue -> config.showPing = newValue)
                    .build());

            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pingstabilizer.option.showJitter"), config.showJitter)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.showJitter"))
                    .setSaveConsumer(newValue -> config.showJitter = newValue)
                    .build());

            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pingstabilizer.option.showPacketLoss"), config.showPacketLoss)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.showPacketLoss"))
                    .setSaveConsumer(newValue -> config.showPacketLoss = newValue)
                    .build());

            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pingstabilizer.option.showPacketStats"), config.showPacketStats)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.showPacketStats"))
                    .setSaveConsumer(newValue -> config.showPacketStats = newValue)
                    .build());
            
            hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.pingstabilizer.option.showNetworkQuality"), config.showNetworkQuality)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.showNetworkQuality"))
                    .setSaveConsumer(newValue -> config.showNetworkQuality = newValue)
                    .build());

            hud.addEntry(entryBuilder.startTextDescription(Text.translatable("config.pingstabilizer.option.openHudPositioner"))
                    .setTooltip(Text.translatable("config.pingstabilizer.tooltip.openHudPositioner"))
                    .build());

            return builder.build();
        };
    }
}


