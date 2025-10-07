package com.davismariotti.physics.collision;

import com.davismariotti.physics.kinematics.Vector;

/**
 * Axis-Aligned Bounding Box collision shape
 * Position represents the center of the box
 */
public record AABBCollider(Vector center, double width, double height) implements Collider {

    /**
     * Get the minimum bounding point (bottom-left corner)
     */
    public Vector getMin() {
        return new Vector(center.x() - width / 2, center.y() - height / 2);
    }

    /**
     * Get the maximum bounding point (top-right corner)
     */
    public Vector getMax() {
        return new Vector(center.x() + width / 2, center.y() + height / 2);
    }
}
