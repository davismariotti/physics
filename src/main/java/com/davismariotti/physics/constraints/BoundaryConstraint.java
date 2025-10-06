package com.davismariotti.physics.constraints;

import com.davismariotti.physics.kinematics.Axis;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.RigidBody;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Constraint that keeps rigid bodies within rectangular boundaries
 */
@Data
@AllArgsConstructor
public class BoundaryConstraint implements Constraint {
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    @Override
    public void apply(RigidBody body, double epsilon) {
        Vector position = body.getPosition();

        // Check X boundaries
        if (position.getX() > maxX) {
            body.setPosition(new Vector(maxX, position.getY()));
            body.flipAboutAxis(Axis.Y);
        } else if (position.getX() < minX) {
            body.setPosition(new Vector(minX, position.getY()));
            body.flipAboutAxis(Axis.Y);
        }

        // Check Y boundaries
        if (position.getY() > maxY) {
            body.setPosition(new Vector(position.getX(), maxY));
            body.flipAboutAxis(Axis.X);
        } else if (position.getY() < minY) {
            body.setPosition(new Vector(position.getX(), minY));
            body.flipAboutAxis(Axis.X);
        }
    }
}
