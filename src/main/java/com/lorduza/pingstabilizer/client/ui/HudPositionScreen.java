package com.lorduza.pingstabilizer.client.ui;

import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.config.NetBoostConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * HUD Position Editor Screen - Individual Element Positioning
 */
public class HudPositionScreen extends Screen {
    
    // Element labels and their current positions (copied from config on init)
    private static final String[] ELEMENT_NAMES = {"Ping", "Jitter", "Loss", "Quality", "PPS"};
    private int[] elementX = new int[5];
    private int[] elementY = new int[5];
    
    // Drag state
    private int selectedElement = -1; // -1 = none selected
    private boolean dragging = false;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    
    // Element dimensions
    private static final int ELEMENT_WIDTH = 80;
    private static final int ELEMENT_HEIGHT = 12;
    
    public HudPositionScreen() {
        super(Text.of("Ping Stabilizer HUD Position"));
        loadPositions();
    }
    
    private void loadPositions() {
        NetBoostConfig config = ConfigManager.get();
        elementX[0] = config.pingX;    elementY[0] = config.pingY;
        elementX[1] = config.jitterX;  elementY[1] = config.jitterY;
        elementX[2] = config.lossX;    elementY[2] = config.lossY;
        elementX[3] = config.qualityX; elementY[3] = config.qualityY;
        elementX[4] = config.ppsX;     elementY[4] = config.ppsY;
    }
    
    private void savePositions() {
        NetBoostConfig config = ConfigManager.get();
        config.pingX = elementX[0];    config.pingY = elementY[0];
        config.jitterX = elementX[1];  config.jitterY = elementY[1];
        config.lossX = elementX[2];    config.lossY = elementY[2];
        config.qualityX = elementX[3]; config.qualityY = elementY[3];
        config.ppsX = elementX[4];     config.ppsY = elementY[4];
        ConfigManager.save();
    }
    
    @Override
    protected void init() {
        // Save button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Save & Close"), button -> {
            saveAndClose();
        }).dimensions(this.width / 2 - 105, this.height - 40, 100, 20).build());
        
        // Reset button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Reset"), button -> {
            resetPositions();
        }).dimensions(this.width / 2 + 5, this.height - 40, 100, 20).build());
    }
    
    private void resetPositions() {
        // Reset to default positions
        elementX[0] = 10; elementY[0] = 10;  // Ping
        elementX[1] = 10; elementY[1] = 22;  // Jitter
        elementX[2] = 10; elementY[2] = 34;  // Loss
        elementX[3] = 10; elementY[3] = 46;  // Quality
        elementX[4] = 10; elementY[4] = 58;  // PPS
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Check mouse state
        boolean mouseDown = GLFW.glfwGetMouseButton(
            this.client.getWindow().getHandle(), 
            GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) == GLFW.GLFW_PRESS;
        
        // Handle mouse click
        if (mouseDown && !dragging) {
            for (int i = 0; i < 5; i++) {
                if (isMouseOverElement(mouseX, mouseY, i)) {
                    selectedElement = i;
                    dragging = true;
                    break;
                }
            }
        }
        
        // Handle mouse release
        if (!mouseDown && dragging) {
            dragging = false;
        }
        
        // Update position while dragging
        if (dragging && selectedElement >= 0) {
            int deltaX = mouseX - lastMouseX;
            int deltaY = mouseY - lastMouseY;
            
            elementX[selectedElement] += deltaX;
            elementY[selectedElement] += deltaY;
            
            // Clamp to screen bounds
            elementX[selectedElement] = Math.max(0, Math.min(elementX[selectedElement], this.width - ELEMENT_WIDTH));
            elementY[selectedElement] = Math.max(0, Math.min(elementY[selectedElement], this.height - ELEMENT_HEIGHT));
        }
        
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        
        // Dark overlay
        context.fill(0, 0, this.width, this.height, 0x80000000);
        
        // Instructions
        context.drawCenteredTextWithShadow(textRenderer, "§b§lDrag HUD Elements", this.width / 2, 20, 0x55FFFF);
        context.drawCenteredTextWithShadow(textRenderer, "§7Click and drag each element separately", this.width / 2, 35, 0xAAAAAA);
        
        // Draw each element with its label
        String[] labels = {"Ping: ~", "Jitter: 0ms", "Loss: 0%", "Quality: Good", "PPS: 123"};
        int[] colors = {0xFFFFFFFF, 0xFF55FF55, 0xFF55FF55, 0xFFFFFF55, 0xFFCCCCCC};
        
        for (int i = 0; i < 5; i++) {
            int x = elementX[i];
            int y = elementY[i];
            
            // Draw border if selected or hovered
            boolean hovered = isMouseOverElement(mouseX, mouseY, i);
            boolean isSelected = (selectedElement == i && dragging);
            
            if (isSelected) {
                // Cyan border when dragging
                drawBorder(context, x - 2, y - 2, x + ELEMENT_WIDTH + 2, y + ELEMENT_HEIGHT + 2, 0xFF55FFFF);
            } else if (hovered) {
                // Yellow border when hovered
                drawBorder(context, x - 2, y - 2, x + ELEMENT_WIDTH + 2, y + ELEMENT_HEIGHT + 2, 0xFFFFFF55);
            }
            
            // Draw the element text
            context.drawTextWithShadow(textRenderer, labels[i], x, y, colors[i]);
        }
        
        // Show selected element name
        if (selectedElement >= 0 && dragging) {
            String info = "§a" + ELEMENT_NAMES[selectedElement] + " dragging...";
            context.drawCenteredTextWithShadow(textRenderer, info, this.width / 2, 55, 0x55FF55);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void drawBorder(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y1 + 1, color);
        context.fill(x1, y2 - 1, x2, y2, color);
        context.fill(x1, y1, x1 + 1, y2, color);
        context.fill(x2 - 1, y1, x2, y2, color);
    }
    
    private boolean isMouseOverElement(int mouseX, int mouseY, int index) {
        int x = elementX[index];
        int y = elementY[index];
        return mouseX >= x - 2 && mouseX <= x + ELEMENT_WIDTH + 2 &&
               mouseY >= y - 2 && mouseY <= y + ELEMENT_HEIGHT + 2;
    }
    
    private void saveAndClose() {
        savePositions();
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }
    
    @Override
    public void close() {
        saveAndClose();
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}
