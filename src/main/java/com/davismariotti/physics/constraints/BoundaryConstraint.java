package com.davismariotti.physics.constraints;

import com.davismariotti.physics.kinematics.Axis;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.DynamicBody;
import com.davismariotti.physics.sprites.RigidBody;

/**
 * Constraint that keeps rigid bodies within side wall boundaries
 * Y boundaries (ground/ceiling) are handled by collision system
 */
public record BoundaryConstraint(double minX, double maxX, double minY, double maxY) implements Constraint {

    @Override
    public void apply(RigidBody body, double epsilon) {
        // Only applies to dynamic bodies
        if (!(body instanceof DynamicBody dynamic)) {
            return;
        }

        Vector position = dynamic.getPosition();
        Vector velocity = dynamic.getVelocity();

        // Check X boundaries (side walls)
        if (position.x() > maxX) {
            dynamic.setPosition(new Vector(maxX, position.y()));
            dynamic.setVelocity(new Vector(-velocity.x(), velocity.y()));
        } else if (position.x() < minX) {
            dynamic.setPosition(new Vector(minX, position.y()));
            dynamic.setVelocity(new Vector(-velocity.x(), velocity.y()));
        }

        // Check Y boundaries
        // Bottom boundary as safety net (collision system handles ground, but this prevents tunneling)
        if (position.y() < minY) {
            dynamic.setPosition(new Vector(position.x(), minY));
            dynamic.setVelocity(new Vector(velocity.x(), -velocity.y()));
        }
        // Top boundary
        if (position.y() > maxY) {
            dynamic.setPosition(new Vector(position.x(), maxY));
            dynamic.setVelocity(new Vector(velocity.x(), -velocity.y()));
        }
    }
}
