package com.davismariotti.physics.forces;

import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.RigidBody;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DragForce implements Force {
    private double dragCoefficient;

    @Override
    public Vector calculate(RigidBody body) {
        if (dragCoefficient <= 0) {
            return Vector.ZERO;
        }

        Vector velocity = body.getVelocity();
        double velocityMagnitude = velocity.getMagnitude();

        if (velocityMagnitude == 0) {
            return Vector.ZERO;
        }

        // F_drag = -dragCoefficient * velocity * |velocity|
        return velocity.multiply(-dragCoefficient * velocityMagnitude);
    }
}
