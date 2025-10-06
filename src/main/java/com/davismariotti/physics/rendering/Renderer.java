package com.davismariotti.physics.rendering;

import com.davismariotti.physics.core.PhysicsConfig;
import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.sprites.Ray;
import com.davismariotti.physics.sprites.RigidBody;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Handles all rendering for the physics simulation
 */
public class Renderer {
    private final int windowWidth;
    private final int windowHeight;
    private final BufferedImage backBuffer;
    private final PhysicsSimulator simulator;
    private final Ray ray;
    private double actualFps;

    public Renderer(int windowWidth, int windowHeight, PhysicsSimulator simulator, Ray ray) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.simulator = simulator;
        this.ray = ray;
        this.backBuffer = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        this.actualFps = 0.0;
    }

    public void setActualFps(double fps) {
        this.actualFps = fps;
    }

    /**
     * Render the entire scene
     * @param targetGraphics the graphics context to draw to
     * @param insets the window insets
     */
    public void render(Graphics targetGraphics, Insets insets) {
        Graphics2D graphics = (Graphics2D) backBuffer.getGraphics();
        AffineTransform at = graphics.getTransform();

        // Flip coordinate system to have origin at bottom-left
        graphics.scale(1, -1);
        graphics.translate(0, -windowHeight);

        // Clear background
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, windowWidth, windowHeight);

        // Draw all rigid bodies
        for (RigidBody body : simulator.getBodies()) {
            body.draw(graphics);
        }

        // Draw the ray (aim indicator)
        ray.draw(graphics);

        // Reset transform for HUD
        graphics.setTransform(at);

        // Draw HUD
        drawHUD(graphics);

        // Copy back buffer to screen
        targetGraphics.drawImage(backBuffer, insets.left, insets.top, null);
    }

    private void drawHUD(Graphics2D graphics) {
        PhysicsConfig config = simulator.getConfig();

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Monospaced", Font.PLAIN, 14));

        String fpsText = String.format("FPS: %.1f", actualFps);
        String restitutionText = String.format("Restitution: %.2f", config.getCoefficientOfRestitution());
        String dragText = String.format("Drag: %.3f", config.getDragCoefficient());

        int textX = windowWidth - 150;
        int textY = 20;
        graphics.drawString(fpsText, textX, textY);
        graphics.drawString(restitutionText, textX, textY + 20);
        graphics.drawString(dragText, textX, textY + 40);
    }
}
