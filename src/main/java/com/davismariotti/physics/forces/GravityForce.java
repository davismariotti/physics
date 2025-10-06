package com.davismariotti.physics.forces;

import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.RigidBody;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GravityForce implements Force {
    private Vector gravity;

    @Override
    public Vector calculate(RigidBody body) {
        return gravity;
    }
}
