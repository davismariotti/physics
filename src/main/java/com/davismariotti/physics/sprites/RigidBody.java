package com.davismariotti.physics.sprites;

import com.davismariotti.physics.collision.Collider;
import com.davismariotti.physics.kinematics.Axis;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.rendering.Camera;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;

import static com.davismariotti.physics.kinematics.Axis.X;

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

    public RigidBody(Vector position, Vector velocity, List<Vector> forces, double mass, boolean isStatic, double coefficientOfRestitution, double dragCoefficient, List<Vector> temporaryForces) {
        this.position = position;
        this.velocity = velocity;
        this.forces = forces;
        this.mass = mass;
        this.isStatic = isStatic;
        this.coefficientOfRestitution = coefficientOfRestitution;
        this.dragCoefficient = dragCoefficient;
        this.temporaryForces = temporaryForces;
    }

    public abstract void draw(Graphics2D graphics, Camera camera);

    /**
     * Get the collision shape for this rigid body
     * @return the collider representing this body's shape
     */
    public abstract Collider getCollider();

    public void update(double epsilon) {
        if (!isStatic) {
            Vector force = this.getResultantForce();

            Vector acceleration = new Vector(force.x() / this.mass, force.y() / this.mass);
            position = position.add(new Vector(velocity.x() * epsilon + .5 * acceleration.x() * epsilon * epsilon, velocity.y() * epsilon + .5 * acceleration.y() * epsilon * epsilon));
            velocity = velocity.add(new Vector(acceleration.x() * epsilon, acceleration.y() * epsilon));
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
            velocity = new Vector(velocity.x(), -coefficientOfRestitution * velocity.y());
        } else {
            velocity = new Vector(-coefficientOfRestitution * velocity.x(), velocity.y());
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

    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public List<Vector> getForces() {
        return forces;
    }

    public void setForces(List<Vector> forces) {
        this.forces = forces;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public double getCoefficientOfRestitution() {
        return coefficientOfRestitution;
    }

    public void setCoefficientOfRestitution(double coefficientOfRestitution) {
        this.coefficientOfRestitution = coefficientOfRestitution;
    }

    public double getDragCoefficient() {
        return dragCoefficient;
    }

    public void setDragCoefficient(double dragCoefficient) {
        this.dragCoefficient = dragCoefficient;
    }

    public List<Vector> getTemporaryForces() {
        return temporaryForces;
    }

    public void setTemporaryForces(List<Vector> temporaryForces) {
        this.temporaryForces = temporaryForces;
    }
}
