package com.lorduza.pingstabilizer.lib;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DebugLogger {
    
    private static BufferedWriter writer;
    private static boolean enabled = false;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    public static void enable() {
        if (enabled) return;
        
        try {
            String desktop = System.getProperty("user.home") + "\\Desktop";
            Path logPath = Paths.get(desktop, "pingstabilizer_debug.txt");
            writer = new BufferedWriter(new FileWriter(logPath.toFile(), false));
            enabled = true;
            
            log("=== Ping Stabilizer Debug Log Started ===");
            log("Time: " + LocalDateTime.now());
            log("=====================================");
        } catch (IOException e) {
            System.err.println("Failed to create debug log: " + e.getMessage());
        }
    }
    
    public static void disable() {
        if (!enabled) return;
        
        try {
            if (writer != null) {
                log("=== Debug Log Ended ===");
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to close debug log: " + e.getMessage());
        }
        enabled = false;
    }
    
    public static void log(String message) {
        if (!enabled || writer == null) return;
        
        try {
            String timestamp = LocalDateTime.now().format(formatter);
            writer.write("[" + timestamp + "] " + message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to write debug log: " + e.getMessage());
        }
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
}


