package com.davismariotti.physics.collision;

import com.davismariotti.physics.kinematics.Vector;

/**
 * Represents the result of a continuous collision detection (TOI) query
 * Contains the exact time and state when two bodies first make contact
 *
 * @param t Time of impact as a fraction [0.0, 1.0] of the timestep. -1.0 indicates no collision.
 * @param position Position of the dynamic body at time of impact
 * @param velocity Velocity of the dynamic body at time of impact
 * @param normal Collision normal pointing from static body toward dynamic body
 */
public record TimeOfImpact(double t, Vector position, Vector velocity, Vector normal) {
    /**
     * Constant representing no collision found
     */
    public static final TimeOfImpact NO_COLLISION = new TimeOfImpact(-1.0, Vector.ZERO, Vector.ZERO, Vector.ZERO);

    /**
     * Check if this represents a valid collision
     */
    public boolean hasCollision() {
        return t >= 0.0;
    }

    /**
     * Create a collision result with the given parameters
     */
    public static TimeOfImpact collision(double t, Vector position, Vector velocity, Vector normal) {
        return new TimeOfImpact(t, position, velocity, normal);
    }
}
