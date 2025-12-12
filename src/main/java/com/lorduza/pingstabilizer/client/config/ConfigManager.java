package com.lorduza.pingstabilizer.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lorduza.pingstabilizer.PingStabilizerMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("netboost.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private static PingStabilizerConfig instance = new PingStabilizerConfig();

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                instance = GSON.fromJson(reader, PingStabilizerConfig.class);
                if (instance == null) {
                    instance = new PingStabilizerConfig();
                }
            } catch (Exception e) {
                PingStabilizerMod.LOGGER.error("Failed to load config", e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            PingStabilizerMod.LOGGER.error("Failed to save config", e);
        }
    }

    public static PingStabilizerConfig get() {
        return instance;
    }
}


