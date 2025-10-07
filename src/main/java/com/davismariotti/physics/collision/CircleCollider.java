package com.davismariotti.physics.collision;

import com.davismariotti.physics.kinematics.Vector;

/**
 * Circular collision shape
 */
public record CircleCollider(Vector center, double radius) implements Collider {

    /**
     * Get the minimum bounding point (bottom-left corner of bounding box)
     */
    public Vector getMin() {
        return new Vector(center.x() - radius, center.y() - radius);
    }

    /**
     * Get the maximum bounding point (top-right corner of bounding box)
     */
    public Vector getMax() {
        return new Vector(center.x() + radius, center.y() + radius);
    }
}
