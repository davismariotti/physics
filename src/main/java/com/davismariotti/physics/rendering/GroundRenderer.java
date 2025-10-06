package com.davismariotti.physics.rendering;

import com.davismariotti.physics.kinematics.Vector;

import java.awt.*;

/**
 * Renders the ground level visualization
 */
public class GroundRenderer implements RenderComponent {
    private final int windowWidth;
    private final double groundY; // ground level in world coordinates
    private final Camera camera;

    public GroundRenderer(int windowWidth, double groundY, Camera camera) {
        this.windowWidth = windowWidth;
        this.groundY = groundY;
        this.camera = camera;
    }

    @Override
    public void render(Graphics2D graphics) {
        // Convert ground Y to screen coordinates
        int screenY = camera.worldToScreen(new Vector(0, groundY)).y();

        // Draw ground as a thick line
        graphics.setColor(new Color(100, 200, 100)); // Green grass color
        graphics.setStroke(new BasicStroke(3));
        graphics.drawLine(0, screenY, windowWidth, screenY);

        // Draw ground fill below the line
        graphics.setColor(new Color(139, 69, 19)); // Brown dirt color
        graphics.fillRect(0, screenY, windowWidth, 5);
    }
}
