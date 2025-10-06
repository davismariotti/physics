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
    private double coefficientOfRestitution;
    private double dragCoefficient;

    public abstract void draw(Graphics2D graphics);

    public void update(double epsilon) {
        if (!isStatic) {
            Vector force = this.getResultantForce();

            // Apply drag force: F_drag = -dragCoefficient * velocity * |velocity|
            if (dragCoefficient > 0) {
                double velocityMagnitude = velocity.getMagnitude();
                if (velocityMagnitude > 0) {
                    Vector dragForce = velocity.multiply(-dragCoefficient * velocityMagnitude);
                    force = force.add(dragForce);
                }
            }

            Vector acceleration = new Vector(force.getX() / this.getMass(), force.getY() / this.getMass());
            setPosition(getPosition().add(new Vector(getVelocity().getX() * epsilon + .5 * acceleration.getX() * epsilon * epsilon, getVelocity().getY() * epsilon + .5 * acceleration.getY() * epsilon * epsilon)));
            setVelocity(getVelocity().add(new Vector(acceleration.getX() * epsilon, acceleration.getY() * epsilon)));
        }
    }

    public void flipAboutAxis(Axis axis) {
        if (axis == X) {
            setVelocity(new Vector(getVelocity().getX(), -coefficientOfRestitution * getVelocity().getY()));
        } else {
            setVelocity(new Vector(-coefficientOfRestitution * getVelocity().getX(), getVelocity().getY()));
        }
    }

    public double getKineticEnergy() {
        double magnitude = velocity.getMagnitude();
        return .5 * mass * magnitude * magnitude;
    }

    public double getPotentialEnergy() {
        return mass * -Game.GRAVITY.getY() * position.getY();
    }

    public double getTotalEnergy() {
        return getKineticEnergy() + getPotentialEnergy();
    }

    public Vector getResultantForce() {
        return forces.stream().reduce(new Vector(0, 0), Vector::add);
    }
}
