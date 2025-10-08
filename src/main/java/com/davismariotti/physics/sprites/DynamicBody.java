package com.davismariotti.physics.sprites;

import com.davismariotti.physics.kinematics.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for dynamic rigid bodies that move and respond to forces
 * Stores previous state for continuous collision detection
 */
public abstract non-sealed class DynamicBody implements RigidBody {
    private Vector position;
    private Vector velocity;
    private double mass;

    private Vector previousPosition;
    private Vector previousVelocity;

    private List<Vector> forces;
    private MaterialProperties material;

    private transient List<Vector> temporaryForces;

    // Sleep state for performance optimization
    private boolean sleeping = false;
    private int restingFrames = 0;

    public DynamicBody(Vector position, Vector velocity, List<Vector> forces, double mass,
                       MaterialProperties material) {
        this.position = position;
        this.velocity = velocity;
        this.mass = mass;
        this.forces = forces;
        this.material = material;
        this.temporaryForces = new ArrayList<>();

        this.previousPosition = position;
        this.previousVelocity = velocity;
    }

    /**
     * Store current state as previous state (called at beginning of substep)
     */
    public void storePreviousState() {
        this.previousPosition = this.position;
        this.previousVelocity = this.velocity;
    }

    /**
     * Update physics state by integrating over time step
     */
    public void update(double epsilon) {
        Vector force = this.getResultantForce();
        Vector acceleration = new Vector(force.x() / this.mass, force.y() / this.mass);

        position = position.add(new Vector(
                velocity.x() * epsilon + 0.5 * acceleration.x() * epsilon * epsilon,
                velocity.y() * epsilon + 0.5 * acceleration.y() * epsilon * epsilon
        ));
        velocity = velocity.add(new Vector(
                acceleration.x() * epsilon,
                acceleration.y() * epsilon
        ));
    }

    /**
     * Add a temporary force (cleared each frame)
     */
    public void addForce(Vector force) {
        temporaryForces.add(force);
    }

    /**
     * Clear all temporary forces
     */
    public void clearTemporaryForces() {
        temporaryForces.clear();
    }

    /**
     * Calculate the resultant force from all forces acting on this body
     */
    private Vector getResultantForce() {
        double x = 0;
        double y = 0;

        for (Vector force : forces) {
            x += force.x();
            y += force.y();
        }

        if (temporaryForces != null) {
            for (Vector force : temporaryForces) {
                x += force.x();
                y += force.y();
            }
        }

        return new Vector(x, y);
    }

    @Override
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

    public double getMass() {
        return mass;
    }

    public Vector getPreviousPosition() {
        return previousPosition;
    }

    public void setPreviousPosition(Vector previousPosition) {
        this.previousPosition = previousPosition;
    }

    public Vector getPreviousVelocity() {
        return previousVelocity;
    }

    public void setPreviousVelocity(Vector previousVelocity) {
        this.previousVelocity = previousVelocity;
    }

    public MaterialProperties getMaterial() {
        return material;
    }

    public void setMaterial(MaterialProperties material) {
        this.material = material;
    }

    @Override
    public double getCoefficientOfRestitution() {
        return material.coefficientOfRestitution();
    }

    public void setCoefficientOfRestitution(double coefficientOfRestitution) {
        this.material = new MaterialProperties(
                coefficientOfRestitution,
                material.dragCoefficient(),
                material.staticFriction(),
                material.dynamicFriction()
        );
    }

    public double getDragCoefficient() {
        return material.dragCoefficient();
    }

    public void setDragCoefficient(double dragCoefficient) {
        this.material = new MaterialProperties(
                material.coefficientOfRestitution(),
                dragCoefficient,
                material.staticFriction(),
                material.dynamicFriction()
        );
    }

    public double getStaticFriction() {
        return material.staticFriction();
    }

    public double getDynamicFriction() {
        return material.dynamicFriction();
    }

    /**
     * Check if this body is sleeping (inactive for performance)
     */
    public boolean isSleeping() {
        return sleeping;
    }

    /**
     * Put this body to sleep (skip physics until woken)
     */
    public void sleep() {
        this.sleeping = true;
        // Zero out velocity when sleeping to prevent drift
        this.velocity = Vector.ZERO;
    }

    /**
     * Wake this body (resume normal physics)
     */
    public void wake() {
        this.sleeping = false;
        this.restingFrames = 0;
    }

    /**
     * Update sleep state based on velocity
     * Should be called after physics update
     *
     * @param velocityThreshold velocity below which body can sleep
     * @param framesRequired consecutive low-velocity frames required to sleep
     */
    public void updateSleepState(double velocityThreshold, int framesRequired) {
        if (sleeping) {
            // Already sleeping, stay asleep until explicitly woken
            return;
        }

        // Calculate velocity magnitude
        double speed = Math.sqrt(velocity.x() * velocity.x() + velocity.y() * velocity.y());

        if (speed < velocityThreshold) {
            restingFrames++;
            if (restingFrames >= framesRequired) {
                sleep();
            }
        } else {
            // Moving too fast, reset counter
            restingFrames = 0;
        }
    }
}
