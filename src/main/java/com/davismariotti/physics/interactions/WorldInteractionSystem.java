package com.davismariotti.physics.interactions;

import com.davismariotti.physics.rendering.Camera;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages world interactions - components that interact with the physics world
 */
public class WorldInteractionSystem {
    private final List<WorldInteraction> interactions;

    public WorldInteractionSystem() {
        this.interactions = new ArrayList<>();
    }

    /**
     * Add an interaction to the system
     */
    public void addInteraction(WorldInteraction interaction) {
        interactions.add(interaction);
    }

    /**
     * Remove an interaction from the system
     */
    public void removeInteraction(WorldInteraction interaction) {
        interactions.remove(interaction);
    }

    /**
     * Update all interactions
     */
    public void update(double epsilon) {
        for (WorldInteraction interaction : interactions) {
            interaction.update(epsilon);
        }
    }

    /**
     * Process input for all interactions
     */
    public void handleInput(InputContext context) {
        for (WorldInteraction interaction : interactions) {
            interaction.handleInput(context);
        }
    }

    /**
     * Draw all interactions
     */
    public void draw(Graphics2D graphics, Camera camera) {
        for (WorldInteraction interaction : interactions) {
            interaction.draw(graphics, camera);
        }
    }

    public List<WorldInteraction> getInteractions() {
        return interactions;
    }
}
