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
    private boolean isStatic;

    public abstract void draw(Graphics2D graphics);

    public void update(double epsilon) {
        if (!isStatic) {
            Vector force = this.getResultantForce();
            Vector acceleration = new Vector(force.getX() / this.getMass(), force.getY() / this.getMass());
            setPosition(getPosition().add(new Vector(getVelocity().getX() * epsilon + .5 * acceleration.getX() * Math.pow(epsilon, 2), getVelocity().getY() * epsilon + .5 * acceleration.getY() * Math.pow(epsilon, 2))));
            setVelocity(getVelocity().add(new Vector(acceleration.getX() * epsilon, acceleration.getY() * epsilon)));
        }
    }

    public void flipAboutAxis(Axis axis) {
        if (axis == X) {
            setVelocity(new Vector(getVelocity().getX(), -.9 * getVelocity().getY()));
        } else {
            setVelocity(new Vector(-.9 * getVelocity().getX(), getVelocity().getY()));
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
