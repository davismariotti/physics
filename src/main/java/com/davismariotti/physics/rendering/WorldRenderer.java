package com.davismariotti.physics.rendering;

import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.sprites.Ray;
import com.davismariotti.physics.sprites.RigidBody;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Renders the physics world (bodies, ray) in world-space coordinates
 */
public class WorldRenderer implements RenderComponent {
    private final PhysicsSimulator simulator;
    private final Ray ray;
    private final int windowHeight;

    public WorldRenderer(PhysicsSimulator simulator, Ray ray, int windowHeight) {
        this.simulator = simulator;
        this.ray = ray;
        this.windowHeight = windowHeight;
    }

    @Override
    public void render(Graphics2D graphics) {
        // Save original transform
        AffineTransform originalTransform = graphics.getTransform();

        // Apply world-space transformation (flip Y-axis)
        graphics.scale(1, -1);
        graphics.translate(0, -windowHeight);

        // Render all rigid bodies
        for (RigidBody body : simulator.getBodies()) {
            body.draw(graphics);
        }

        // Render the ray (aim indicator)
        ray.draw(graphics);

        // Restore original transform
        graphics.setTransform(originalTransform);
    }
}
