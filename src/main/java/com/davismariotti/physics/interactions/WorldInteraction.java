package com.davismariotti.physics.interactions;

import com.davismariotti.physics.rendering.Camera;

import java.awt.*;

/**
 * Interface for interactive components that can interact with the physics world
 */
public interface WorldInteraction {
    /**
     * Update this interaction
     * @param epsilon time step
     */
    void update(double epsilon);

    /**
     * Handle input for this interaction
     * @param context provides access to input state and world
     */
    void handleInput(InputContext context);

    /**
     * Draw this interaction in world space
     * @param graphics the graphics context
     * @param camera the camera for converting world to screen coordinates
     */
    void draw(Graphics2D graphics, Camera camera);
}
