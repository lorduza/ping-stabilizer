package com.lorduza.pingstabilizer.client.ui;

import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.config.PingStabilizerConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;


public class HudPositionScreen extends Screen {

    private int[] elementX = new int[4];
    private int[] elementY = new int[4];

    private int selectedElement = -1; // -1 = none selected
    private boolean dragging = false;
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    private static final int ELEMENT_WIDTH = 80;
    private static final int ELEMENT_HEIGHT = 12;
    
    public HudPositionScreen() {
        super(Text.translatable("hud.pingstabilizer.position_screen.title"));
        loadPositions();
    }
    
    private void loadPositions() {
        PingStabilizerConfig config = ConfigManager.get();
        elementX[0] = config.jitterX;  elementY[0] = config.jitterY;
        elementX[1] = config.lossX;    elementY[1] = config.lossY;
        elementX[2] = config.qualityX; elementY[2] = config.qualityY;
        elementX[3] = config.ppsX;     elementY[3] = config.ppsY;
    }
    
    private void savePositions() {
        PingStabilizerConfig config = ConfigManager.get();
        config.jitterX = elementX[0];  config.jitterY = elementY[0];
        config.lossX = elementX[1];    config.lossY = elementY[1];
        config.qualityX = elementX[2]; config.qualityY = elementY[2];
        config.ppsX = elementX[3];     config.ppsY = elementY[3];
        ConfigManager.save();
    }
    
    @Override
    protected void init() {

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("hud.pingstabilizer.position_screen.save_close"), button -> {
            saveAndClose();
        }).dimensions(this.width / 2 - 105, this.height - 40, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("hud.pingstabilizer.position_screen.reset"), button -> {
            resetPositions();
        }).dimensions(this.width / 2 + 5, this.height - 40, 100, 20).build());
    }
    
    private void resetPositions() {

        elementX[0] = 10; elementY[0] = 22;  // Jitter
        elementX[1] = 10; elementY[1] = 34;  // Loss
        elementX[2] = 10; elementY[2] = 46;  // Quality
        elementX[3] = 10; elementY[3] = 58;  // PPS
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        boolean mouseDown = GLFW.glfwGetMouseButton(
            this.client.getWindow().getHandle(), 
            GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) == GLFW.GLFW_PRESS;

        if (mouseDown && !dragging) {
            for (int i = 0; i < 4; i++) {
                if (isMouseOverElement(mouseX, mouseY, i)) {
                    selectedElement = i;
                    dragging = true;
                    break;
                }
            }
        }

        if (!mouseDown && dragging) {
            dragging = false;
        }

        if (dragging && selectedElement >= 0) {
            int deltaX = mouseX - lastMouseX;
            int deltaY = mouseY - lastMouseY;
            
            elementX[selectedElement] += deltaX;
            elementY[selectedElement] += deltaY;

            elementX[selectedElement] = Math.max(0, Math.min(elementX[selectedElement], this.width - ELEMENT_WIDTH));
            elementY[selectedElement] = Math.max(0, Math.min(elementY[selectedElement], this.height - ELEMENT_HEIGHT));
        }
        
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        context.fill(0, 0, this.width, this.height, 0x80000000);

        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("hud.pingstabilizer.position_screen.drag_title"), this.width / 2, 20, 0x55FFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("hud.pingstabilizer.position_screen.drag_subtitle"), this.width / 2, 35, 0xAAAAAA);

        Text[] labels = {
            Text.translatable("hud.pingstabilizer.label.jitter", "0ms"),
            Text.translatable("hud.pingstabilizer.label.loss", "0%"),
            Text.translatable("hud.pingstabilizer.label.quality", Text.translatable("hud.pingstabilizer.quality.good")),
            Text.translatable("hud.pingstabilizer.label.pps", "123")
        };
        int[] colors = {0xFF55FF55, 0xFF55FF55, 0xFFFFFF55, 0xFFCCCCCC};
        
        for (int i = 0; i < 4; i++) {
            int x = elementX[i];
            int y = elementY[i];

            boolean hovered = isMouseOverElement(mouseX, mouseY, i);
            boolean isSelected = (selectedElement == i && dragging);
            
            if (isSelected) {

                drawBorder(context, x - 2, y - 2, x + ELEMENT_WIDTH + 2, y + ELEMENT_HEIGHT + 2, 0xFF55FFFF);
            } else if (hovered) {

                drawBorder(context, x - 2, y - 2, x + ELEMENT_WIDTH + 2, y + ELEMENT_HEIGHT + 2, 0xFFFFFF55);
            }

            context.drawTextWithShadow(textRenderer, labels[i], x, y, colors[i]);
        }

        if (selectedElement >= 0 && dragging) {
            Text info = Text.translatable("hud.pingstabilizer.position_screen.dragging", labels[selectedElement].getString());
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


