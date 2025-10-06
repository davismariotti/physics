package com.davismariotti.physics.rendering;

import java.awt.*;

/**
 * Interface for rendering components that can draw themselves
 */
public interface RenderComponent {
    /**
     * Render this component
     * @param graphics the graphics context to draw with
     */
    void render(Graphics2D graphics);
}
