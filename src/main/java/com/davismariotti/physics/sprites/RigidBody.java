package com.davismariotti.physics.sprites;

import com.davismariotti.physics.Game;
import com.davismariotti.physics.kinematics.Axis;
import com.davismariotti.physics.kinematics.Vector;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.awt.*;

import static com.davismariotti.physics.kinematics.Axis.X;

@Data
@AllArgsConstructor
public abstract class RigidBody {
    private Vector position;
    private Vector velocity;
    private List<Vector> forces;
    private double mass;

    public abstract void draw(Graphics2D graphics);

    public abstract void update(double epsilon);

    public void flipAboutAxis(Axis axis) {
        if (axis == X) {
            velocity = velocity.multiply(1, -1);
        } else {
            velocity = velocity.multiply(-1, 1);
        }
    }

    public double getKeneticEnergy() {
        return Math.abs(.5 * mass * Math.pow(velocity.getMagnitude(), 2));
    }

    public double getPotentialEnergy() {
        return Math.abs(mass * Game.GRAVITY.getY() * position.getY());
    }

    public double getTotalEnergy() {
        return getKeneticEnergy() + getPotentialEnergy();
    }

    public Vector getResultantForce() {
        return forces.stream().reduce(new Vector(0, 0), Vector::add);
    }
}
