package com.davismariotti.physics.constraints;

import com.davismariotti.physics.sprites.RigidBody;

/**
 * Interface for physics constraints that can be applied to rigid bodies
 */
public interface Constraint {
    /**
     * Apply this constraint to the given rigid body
     * @param body the rigid body to constrain
     * @param epsilon the time step
     */
    void apply(RigidBody body, double epsilon);
}
