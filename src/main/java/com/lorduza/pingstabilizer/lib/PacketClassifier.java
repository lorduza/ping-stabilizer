package com.lorduza.pingstabilizer.lib;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.common.*;


public class PacketClassifier {
    
    public enum Category {
        CRITICAL,   // Combat, movement - NEVER delay
        NORMAL,     // Standard gameplay packets
        BULK        // Can be delayed during congestion
    }
    
    
    public static Category classify(Packet<?> packet) {
        if (packet == null) return Category.NORMAL;



        if (packet instanceof PlayerInteractEntityC2SPacket) {
            return Category.CRITICAL;
        }

        if (packet instanceof PlayerActionC2SPacket) {
            return Category.CRITICAL;
        }

        if (packet instanceof PlayerInteractBlockC2SPacket) {
            return Category.CRITICAL;
        }

        if (packet instanceof PlayerInteractItemC2SPacket) {
            return Category.CRITICAL;
        }

        if (packet instanceof HandSwingC2SPacket) {
            return Category.CRITICAL;
        }

        if (packet instanceof UpdateSelectedSlotC2SPacket) {
            return Category.CRITICAL;
        }

        if (packet instanceof PlayerMoveC2SPacket) {
            return Category.CRITICAL;
        }

        if (packet instanceof VehicleMoveC2SPacket) {
            return Category.CRITICAL;
        }

        if (packet instanceof TeleportConfirmC2SPacket) {
            return Category.CRITICAL;
        }

        if (packet instanceof KeepAliveC2SPacket) {
            return Category.CRITICAL;
        }



        if (packet instanceof ClientStatusC2SPacket) {
            return Category.BULK;
        }

        if (packet instanceof ChatMessageC2SPacket) {
            return Category.BULK;
        }

        if (packet instanceof CommandExecutionC2SPacket) {
            return Category.NORMAL;
        }

        if (packet instanceof ResourcePackStatusC2SPacket) {
            return Category.BULK;
        }

        if (packet instanceof CustomPayloadC2SPacket) {
            return Category.NORMAL;
        }

        return Category.NORMAL;
    }
    
    
    public static boolean isCritical(Packet<?> packet) {
        return classify(packet) == Category.CRITICAL;
    }
    
    
    public static boolean canDelay(Packet<?> packet) {
        return classify(packet) == Category.BULK;
    }
    
    
    public static int getPriorityScore(Packet<?> packet) {
        switch (classify(packet)) {
            case CRITICAL: return 100;
            case NORMAL: return 50;
            case BULK: return 10;
            default: return 50;
        }
    }
}


