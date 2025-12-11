package com.lorduza.pingstabilizer.lib;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.common.*;

/**
 * Packet Classifier - Categorizes packets for traffic management
 * 
 * CRITICAL: Must be sent immediately (combat, movement)
 * NORMAL: Standard priority
 * BULK: Can be delayed when network is congested (chunks, skins, chat)
 */
public class PacketClassifier {
    
    public enum Category {
        CRITICAL,   // Combat, movement - NEVER delay
        NORMAL,     // Standard gameplay packets
        BULK        // Can be delayed during congestion
    }
    
    /**
     * Classify a packet into a category
     */
    public static Category classify(Packet<?> packet) {
        if (packet == null) return Category.NORMAL;
        
        // ========== CRITICAL - Combat, Movement & Sync ==========
        // These packets MUST be sent immediately - NEVER delay
        
        // Combat packets - hit registration
        if (packet instanceof PlayerInteractEntityC2SPacket) {
            return Category.CRITICAL;
        }
        
        // Actions - block break/place, item use, attack
        if (packet instanceof PlayerActionC2SPacket) {
            return Category.CRITICAL;
        }
        
        // Block interaction - crystal placement, chest opening
        if (packet instanceof PlayerInteractBlockC2SPacket) {
            return Category.CRITICAL;
        }
        
        // Item use - totem, ender pearl, potions
        if (packet instanceof PlayerInteractItemC2SPacket) {
            return Category.CRITICAL;
        }
        
        // Attack animation - important for hit sync
        if (packet instanceof HandSwingC2SPacket) {
            return Category.CRITICAL;
        }
        
        // Hotbar slot - totem swapping
        if (packet instanceof UpdateSelectedSlotC2SPacket) {
            return Category.CRITICAL;
        }
        
        // Movement - position updates
        if (packet instanceof PlayerMoveC2SPacket) {
            return Category.CRITICAL;
        }
        
        // Vehicle movement
        if (packet instanceof VehicleMoveC2SPacket) {
            return Category.CRITICAL;
        }
        
        // Teleport confirmation - CRITICAL for sync (desync if delayed!)
        if (packet instanceof TeleportConfirmC2SPacket) {
            return Category.CRITICAL;
        }
        
        // Keep-alive - important for connection stability
        if (packet instanceof KeepAliveC2SPacket) {
            return Category.CRITICAL;
        }
        
        // ========== BULK - Can be delayed ==========
        // These don't affect combat and can wait when network is congested
        
        // Client status (respawn screen, stats)
        if (packet instanceof ClientStatusC2SPacket) {
            return Category.BULK;
        }
        
        // Chat messages - can wait
        if (packet instanceof ChatMessageC2SPacket) {
            return Category.BULK;
        }
        
        // Command execution - can wait slightly
        if (packet instanceof CommandExecutionC2SPacket) {
            return Category.BULK;
        }
        
        // Resource pack response
        if (packet instanceof ResourcePackStatusC2SPacket) {
            return Category.BULK;
        }
        
        // Custom payload (plugin messages)
        if (packet instanceof CustomPayloadC2SPacket) {
            return Category.BULK;
        }
        
        // Default to NORMAL (safe middle ground)
        return Category.NORMAL;
    }
    
    /**
     * Check if packet is combat-critical
     */
    public static boolean isCritical(Packet<?> packet) {
        return classify(packet) == Category.CRITICAL;
    }
    
    /**
     * Check if packet can be delayed during congestion
     */
    public static boolean canDelay(Packet<?> packet) {
        return classify(packet) == Category.BULK;
    }
    
    /**
     * Get priority score (higher = more important)
     */
    public static int getPriorityScore(Packet<?> packet) {
        switch (classify(packet)) {
            case CRITICAL: return 100;
            case NORMAL: return 50;
            case BULK: return 10;
            default: return 50;
        }
    }
}
