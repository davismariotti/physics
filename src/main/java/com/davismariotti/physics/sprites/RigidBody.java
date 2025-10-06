package com.davismariotti.physics.sprites;

import com.davismariotti.physics.kinematics.Axis;
import com.davismariotti.physics.kinematics.Vector;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
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

    // Temporary forces that are cleared each frame (for global forces like drag)
    private transient List<Vector> temporaryForces;

    public abstract void draw(Graphics2D graphics);

    public void update(double epsilon) {
        if (!isStatic) {
            Vector force = this.getResultantForce();

            Vector acceleration = new Vector(force.getX() / this.getMass(), force.getY() / this.getMass());
            setPosition(getPosition().add(new Vector(getVelocity().getX() * epsilon + .5 * acceleration.getX() * epsilon * epsilon, getVelocity().getY() * epsilon + .5 * acceleration.getY() * epsilon * epsilon)));
            setVelocity(getVelocity().add(new Vector(acceleration.getX() * epsilon, acceleration.getY() * epsilon)));
        }
    }

    /**
     * Add a temporary force that will be cleared after this frame
     */
    public void addForce(Vector force) {
        if (temporaryForces == null) {
            temporaryForces = new ArrayList<>();
        }
        temporaryForces.add(force);
    }

    /**
     * Clear temporary forces (called after each physics update)
     */
    public void clearTemporaryForces() {
        if (temporaryForces != null) {
            temporaryForces.clear();
        }
    }

    public void flipAboutAxis(Axis axis) {
        if (axis == X) {
            setVelocity(new Vector(getVelocity().getX(), -coefficientOfRestitution * getVelocity().getY()));
        } else {
            setVelocity(new Vector(-coefficientOfRestitution * getVelocity().getX(), getVelocity().getY()));
        }
    }

    public Vector getResultantForce() {
        Vector result = forces.stream().reduce(new Vector(0, 0), Vector::add);

        // Add temporary forces
        if (temporaryForces != null) {
            result = temporaryForces.stream().reduce(result, Vector::add);
        }

        return result;
    }
}
