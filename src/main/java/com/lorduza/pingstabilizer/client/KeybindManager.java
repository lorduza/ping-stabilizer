package com.lorduza.pingstabilizer.client;

import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.ui.HudPositionScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;


public class KeybindManager {

    private static final KeyBinding.Category CATEGORY = new KeyBinding.Category(
        Identifier.of("pingstabilizer", "keybinds")
    );
    
    private static KeyBinding hudPositionKey;
    private static KeyBinding toggleHudKey;
    
    public static void register() {

        hudPositionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pingstabilizer.hud_position",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
        ));

        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pingstabilizer.toggle_hud",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            CATEGORY
        ));
    }
    
    public static void tick(MinecraftClient client) {

        while (hudPositionKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new HudPositionScreen());
            }
        }

        while (toggleHudKey.wasPressed()) {
            var config = ConfigManager.get();
            config.hudEnabled = !config.hudEnabled;
            ConfigManager.save();

            if (client.player != null) {
                String status = config.hudEnabled ? "§aON" : "§cOFF";
                client.player.sendMessage(Text.of("§b[PingStabilizer]§r HUD: " + status), true);
            }
        }
    }
}




