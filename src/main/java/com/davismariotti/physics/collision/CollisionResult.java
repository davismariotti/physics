package com.davismariotti.physics.collision;

import com.davismariotti.physics.kinematics.Vector;

/**
 * Result of a collision detection check
 */
public record CollisionResult(
        boolean hasCollision,
        Vector normal,           // Unit vector pointing from surface into the colliding object
        double penetrationDepth  // How far the object has penetrated into the surface
) {
    /**
     * No collision occurred
     */
    public static final CollisionResult NO_COLLISION = new CollisionResult(false, Vector.ZERO, 0.0);

    /**
     * Create a collision result
     */
    public static CollisionResult collision(Vector normal, double penetrationDepth) {
        return new CollisionResult(true, normal, penetrationDepth);
    }
}
