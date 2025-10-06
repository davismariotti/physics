package com.davismariotti.physics.forces;

import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.RigidBody;

public record GravityForce(Vector gravity) implements Force {

    @Override
    public Vector calculate(RigidBody body) {
        return gravity;
    }
}
