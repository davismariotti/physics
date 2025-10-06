package com.davismariotti.physics.rendering;

import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.sprites.Ray;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates rendering by composing multiple render components
 */
public class Renderer {
    private final int windowWidth;
    private final int windowHeight;
    private final BufferedImage backBuffer;
    private final List<RenderComponent> components;
    private final HUDRenderer hudRenderer;

    public Renderer(int windowWidth, int windowHeight, PhysicsSimulator simulator, Ray ray) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.backBuffer = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        this.components = new ArrayList<>();

        // Create and add render components
        WorldRenderer worldRenderer = new WorldRenderer(simulator, ray, windowHeight);
        this.hudRenderer = new HUDRenderer(simulator.getConfig(), windowWidth, windowHeight);

        components.add(worldRenderer);
        components.add(hudRenderer);
    }

    public void setActualFps(double fps) {
        hudRenderer.setFps(fps);
    }

    /**
     * Render the entire scene
     * @param targetGraphics the graphics context to draw to
     * @param insets the window insets
     */
    public void render(Graphics targetGraphics, Insets insets) {
        Graphics2D graphics = (Graphics2D) backBuffer.getGraphics();

        // Clear background
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, windowWidth, windowHeight);

        // Render all components
        for (RenderComponent component : components) {
            component.render(graphics);
        }

        // Copy back buffer to screen
        targetGraphics.drawImage(backBuffer, insets.left, insets.top, null);
    }
}
