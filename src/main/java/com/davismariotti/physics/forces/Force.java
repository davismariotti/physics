package com.davismariotti.physics.forces;

import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.RigidBody;

/**
 * Interface for forces that can be applied to rigid bodies
 */
public interface Force {
    /**
     * Calculate the force vector to apply to the given body
     * @param body the rigid body to calculate force for
     * @return the force vector
     */
    Vector calculate(RigidBody body);
}
