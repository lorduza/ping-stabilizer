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

/**
 * Manages keybindings for NetBooster
 */
public class KeybindManager {
    
    // Category for NetBooster keybindings
    private static final KeyBinding.Category CATEGORY = new KeyBinding.Category(
        Identifier.of("netbooster", "keybinds")
    );
    
    private static KeyBinding hudPositionKey;
    private static KeyBinding toggleHudKey;
    
    public static void register() {
        // H key - Open HUD position editor
        hudPositionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.netbooster.hud_position",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
        ));
        
        // N key - Toggle HUD visibility
        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.netbooster.toggle_hud",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            CATEGORY
        ));
    }
    
    public static void tick(MinecraftClient client) {
        // H - Open HUD position screen
        while (hudPositionKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new HudPositionScreen());
            }
        }
        
        // N - Toggle HUD
        while (toggleHudKey.wasPressed()) {
            var config = ConfigManager.get();
            config.hudEnabled = !config.hudEnabled;
            ConfigManager.save();
            
            // Show feedback
            if (client.player != null) {
                String status = config.hudEnabled ? "§aON" : "§cOFF";
                client.player.sendMessage(Text.of("§b[NetBooster]§r HUD: " + status), true);
            }
        }
    }
}


