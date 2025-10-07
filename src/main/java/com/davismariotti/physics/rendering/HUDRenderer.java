package com.davismariotti.physics.rendering;

import com.davismariotti.physics.constraints.CollisionMetrics;
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
    private CollisionMetrics collisionMetrics;

    public HUDRenderer(PhysicsConfig config, int windowWidth, int windowHeight) {
        this.config = config;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.fps = 0.0;
        this.collisionMetrics = null;
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Monospaced", Font.PLAIN, 14));

        int textX = windowWidth - 200;
        int textY = 20;
        int lineHeight = 20;
        int line = 0;

        // Performance metrics
        graphics.drawString(String.format("FPS: %.1f", fps), textX, textY + (line++ * lineHeight));
        graphics.drawString(String.format("Restitution: %.2f", config.getCoefficientOfRestitution()), textX, textY + (line++ * lineHeight));
        graphics.drawString(String.format("Drag: %.3f", config.getDragCoefficient()), textX, textY + (line++ * lineHeight));

    }

    public void setFps(double fps) {
        this.fps = fps;
    }

    public void setCollisionMetrics(CollisionMetrics metrics) {
        this.collisionMetrics = metrics;
    }
}
