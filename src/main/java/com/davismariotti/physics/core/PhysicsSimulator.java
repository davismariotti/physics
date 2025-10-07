package com.davismariotti.physics.core;

import com.davismariotti.physics.constraints.Constraint;
import com.davismariotti.physics.forces.DragForce;
import com.davismariotti.physics.forces.Force;
import com.davismariotti.physics.forces.GravityForce;
import com.davismariotti.physics.sprites.RigidBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the physics simulation, updating all bodies and applying constraints
 */
public class PhysicsSimulator {
    private final List<RigidBody> bodies;
    private final List<Constraint> constraints;
    private final List<Force> globalForces;
    private final PhysicsConfig config;

    public PhysicsSimulator(PhysicsConfig config) {
        this.bodies = new ArrayList<>();
        this.constraints = new ArrayList<>();
        this.globalForces = new ArrayList<>();
        this.config = config;

        // Set up default global forces
        globalForces.add(new GravityForce(config.getGravity()));
        globalForces.add(new DragForce(config.getDragCoefficient()));
    }

    public void addBody(RigidBody body) {
        bodies.add(body);
    }

    public void removeBody(RigidBody body) {
        bodies.remove(body);
    }

    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }

    public void removeConstraint(Constraint constraint) {
        constraints.remove(constraint);
    }

    /**
     * Update all bodies in the simulation
     * @param epsilon time step
     */
    public void update(double epsilon) {
        // Use sub-stepping for more accurate collision detection
        int substeps = config.getSubsteps();
        double substepDelta = epsilon / substeps;

        for (int step = 0; step < substeps; step++) {
            // Update all bodies for this substep
            for (RigidBody body : bodies) {
                // Apply global forces
                for (Force force : globalForces) {
                    body.addForce(force.calculate(body));
                }

                // Update physics with smaller time step
                body.update(substepDelta);

                // Apply constraints
                for (Constraint constraint : constraints) {
                    constraint.apply(body, substepDelta);
                }

                // Clear temporary forces for next substep
                body.clearTemporaryForces();
            }
        }
    }

    /**
     * Update the drag coefficient for all bodies
     */
    public void updateDragCoefficient(double newDragCoefficient) {
        config.setDragCoefficient(newDragCoefficient);
        // Update the global drag force
        for (int i = 0; i < globalForces.size(); i++) {
            if (globalForces.get(i) instanceof DragForce) {
                globalForces.set(i, new DragForce(newDragCoefficient));
                break;
            }
        }
        // Update all bodies
        for (RigidBody body : bodies) {
            body.setDragCoefficient(newDragCoefficient);
        }
    }

    /**
     * Update the coefficient of restitution for all bodies
     */
    public void updateCoefficientOfRestitution(double newCoefficient) {
        config.setCoefficientOfRestitution(newCoefficient);
        for (RigidBody body : bodies) {
            body.setCoefficientOfRestitution(newCoefficient);
        }
    }

    public PhysicsConfig getConfig() {
        return config;
    }

    public List<RigidBody> getBodies() {
        return bodies;
    }
}
