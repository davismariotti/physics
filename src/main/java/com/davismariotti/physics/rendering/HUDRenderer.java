package com.davismariotti.physics.rendering;

import com.davismariotti.physics.core.PhysicsConfig;

import java.awt.*;

/**
 * Renders HUD overlay information in screen-space coordinates
 */
public class HUDRenderer implements RenderComponent {
    private final PhysicsConfig config;
    private final int windowWidth;
    private final int windowHeight;

    private double fps;

    public HUDRenderer(PhysicsConfig config, int windowWidth, int windowHeight) {
        this.config = config;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.fps = 0.0;
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Monospaced", Font.PLAIN, 14));

        String fpsText = String.format("FPS: %.1f", fps);
        String restitutionText = String.format("Restitution: %.2f", config.getCoefficientOfRestitution());
        String dragText = String.format("Drag: %.3f", config.getDragCoefficient());

        int textX = windowWidth - 150;
        int textY = 20;
        graphics.drawString(fpsText, textX, textY);
        graphics.drawString(restitutionText, textX, textY + 20);
        graphics.drawString(dragText, textX, textY + 40);
    }

    public void setFps(double fps) {
        this.fps = fps;
    }
}
