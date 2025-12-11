package com.lorduza.pingstabilizer;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingStabilizerMod implements ModInitializer {
    public static final String MOD_ID = "netbooster";
    public static final Logger LOGGER = LoggerFactory.getLogger("Ping Stabilizer");

    @Override
    public void onInitialize() {
        LOGGER.info("Ping Stabilizer initialized (Common)");
    }
}
