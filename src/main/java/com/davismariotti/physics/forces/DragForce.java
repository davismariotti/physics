package com.davismariotti.physics.forces;

import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.DynamicBody;
import com.davismariotti.physics.sprites.RigidBody;

public record DragForce(double dragCoefficient) implements Force {

    @Override
    public Vector calculate(RigidBody body) {
        // Only applies to dynamic bodies
        if (!(body instanceof DynamicBody dynamic)) {
            return Vector.ZERO;
        }

        if (dragCoefficient <= 0) {
            return Vector.ZERO;
        }

        Vector velocity = dynamic.getVelocity();
        double velocityMagnitude = velocity.getMagnitude();

        if (velocityMagnitude == 0) {
            return Vector.ZERO;
        }

        // F_drag = -dragCoefficient * velocity * |velocity|
        return velocity.multiply(-dragCoefficient * velocityMagnitude);
    }
}
