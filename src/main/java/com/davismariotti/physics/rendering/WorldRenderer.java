package com.davismariotti.physics.rendering;

import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.interactions.WorldInteractionSystem;
import com.davismariotti.physics.sprites.RigidBody;

import java.awt.*;

/**
 * Renders the physics world (bodies, interactions) using Camera for coordinate conversion
 */
public class WorldRenderer implements RenderComponent {
    private final PhysicsSimulator simulator;
    private final WorldInteractionSystem interactionSystem;
    private final Camera camera;

    public WorldRenderer(PhysicsSimulator simulator, WorldInteractionSystem interactionSystem, Camera camera) {
        this.simulator = simulator;
        this.interactionSystem = interactionSystem;
        this.camera = camera;
    }

    @Override
    public void render(Graphics2D graphics) {
        // Render all rigid bodies
        for (RigidBody body : simulator.getBodies()) {
            body.draw(graphics, camera);
        }

        // Render all world interactions
        interactionSystem.draw(graphics, camera);
    }
}
