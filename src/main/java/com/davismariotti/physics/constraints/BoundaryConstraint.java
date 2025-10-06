package com.davismariotti.physics.constraints;

import com.davismariotti.physics.kinematics.Axis;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.RigidBody;

/**
 * Constraint that keeps rigid bodies within rectangular boundaries
 */
public record BoundaryConstraint(double minX, double maxX, double minY, double maxY) implements Constraint {

    @Override
    public void apply(RigidBody body, double epsilon) {
        Vector position = body.getPosition();

        // Check X boundaries
        if (position.x() > maxX) {
            body.setPosition(new Vector(maxX, position.y()));
            body.flipAboutAxis(Axis.Y);
        } else if (position.x() < minX) {
            body.setPosition(new Vector(minX, position.y()));
            body.flipAboutAxis(Axis.Y);
        }

        // Check Y boundaries
        if (position.y() > maxY) {
            body.setPosition(new Vector(position.x(), maxY));
            body.flipAboutAxis(Axis.X);
        } else if (position.y() < minY) {
            body.setPosition(new Vector(position.x(), minY));
            body.flipAboutAxis(Axis.X);
        }
    }
}
