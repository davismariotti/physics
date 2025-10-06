package com.davismariotti.physics.rendering;

import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.interactions.WorldInteractionSystem;
import com.davismariotti.physics.sprites.RigidBody;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Renders the physics world (bodies, interactions) in world-space coordinates
 */
public class WorldRenderer implements RenderComponent {
    private final PhysicsSimulator simulator;
    private final WorldInteractionSystem interactionSystem;
    private final int windowHeight;

    public WorldRenderer(PhysicsSimulator simulator, WorldInteractionSystem interactionSystem, int windowHeight) {
        this.simulator = simulator;
        this.interactionSystem = interactionSystem;
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

        // Render all world interactions
        interactionSystem.draw(graphics);

        // Restore original transform
        graphics.setTransform(originalTransform);
    }
}
