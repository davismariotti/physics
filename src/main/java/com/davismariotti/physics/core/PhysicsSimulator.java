package com.davismariotti.physics.core;

import com.davismariotti.physics.constraints.Constraint;
import com.davismariotti.physics.constraints.DynamicCollisionConstraint;
import com.davismariotti.physics.forces.DragForce;
import com.davismariotti.physics.forces.Force;
import com.davismariotti.physics.forces.GravityForce;
import com.davismariotti.physics.sprites.DynamicBody;
import com.davismariotti.physics.sprites.RigidBody;
import com.davismariotti.physics.sprites.StaticBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the physics simulation, updating all bodies and applying constraints
 */
public class PhysicsSimulator {
    private final List<DynamicBody> dynamicBodies;
    private final List<StaticBody> staticBodies;
    private final List<Constraint> constraints;
    private final List<Force> globalForces;
    private final PhysicsConfig config;
    private final DynamicCollisionConstraint dynamicCollisionConstraint;
    private double worldMinX, worldMaxX, worldMinY, worldMaxY;

    public PhysicsSimulator(PhysicsConfig config) {
        this.dynamicBodies = new ArrayList<>();
        this.staticBodies = new ArrayList<>();
        this.constraints = new ArrayList<>();
        this.globalForces = new ArrayList<>();
        this.config = config;

        // Set up default global forces
        globalForces.add(new GravityForce(config.getGravity()));
        globalForces.add(new DragForce(config.getDragCoefficient()));

        // Set up dynamic collision constraint (handles ball-to-ball collisions)
        this.dynamicCollisionConstraint = new DynamicCollisionConstraint(dynamicBodies, config.getGravity());

        // World bounds will be set via setWorldBounds()
        this.worldMinX = 0;
        this.worldMaxX = 100;
        this.worldMinY = 0;
        this.worldMaxY = 80;
    }

    /**
     * Set world bounds for spatial partitioning
     * Should be called after PhysicsSimulator creation with actual game dimensions
     */
    public void setWorldBounds(double minX, double maxX, double minY, double maxY) {
        this.worldMinX = minX;
        this.worldMaxX = maxX;
        this.worldMinY = minY;
        this.worldMaxY = maxY;

        // Update spatial partitioning if enabled
        if (config.isUseSpatialPartitioning()) {
            dynamicCollisionConstraint.enableSpatialPartitioning(
                    worldMinX, worldMaxX, worldMinY, worldMaxY, config.getGridCellSize());
        }
    }

    public void addBody(RigidBody body) {
        if (body instanceof DynamicBody dynamic) {
            dynamicBodies.add(dynamic);
        } else if (body instanceof StaticBody staticBody) {
            staticBodies.add(staticBody);
        }
    }

    public void removeBody(RigidBody body) {
        if (body instanceof DynamicBody dynamic) {
            dynamicBodies.remove(dynamic);
        } else if (body instanceof StaticBody staticBody) {
            staticBodies.remove(staticBody);
        }
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
            // Update all dynamic bodies for this substep
            for (DynamicBody body : dynamicBodies) {
                // Skip sleeping bodies entirely
                if (body.isSleeping()) {
                    continue;
                }

                // Store previous state for TOI calculation
                body.storePreviousState();

                // Apply global forces
                for (Force force : globalForces) {
                    body.addForce(force.calculate(body));
                }

                // Update physics with smaller time step
                body.update(substepDelta);

                // Apply per-body constraints (static collisions, boundaries)
                for (Constraint constraint : constraints) {
                    constraint.apply(body, substepDelta);
                }

                // Clear temporary forces for next substep
                body.clearTemporaryForces();
            }

            // Apply dynamic collision constraint (ball-to-ball collisions)
            // Called once per substep, not per body
            // Uses velocity iterations for better convergence in stacks
            dynamicCollisionConstraint.applyAll(substepDelta, config.getVelocityIterations());
        }

        // Update sleep states after all physics (if sleeping enabled)
        if (config.isUseSleeping()) {
            for (DynamicBody body : dynamicBodies) {
                body.updateSleepState(config.getSleepVelocityThreshold(), config.getSleepFramesRequired());
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
        // Update all dynamic bodies
        for (DynamicBody body : dynamicBodies) {
            body.setDragCoefficient(newDragCoefficient);
        }
    }

    /**
     * Update the coefficient of restitution for all bodies
     */
    public void updateCoefficientOfRestitution(double newCoefficient) {
        config.setCoefficientOfRestitution(newCoefficient);
        for (DynamicBody body : dynamicBodies) {
            body.setCoefficientOfRestitution(newCoefficient);
        }
    }

    public PhysicsConfig getConfig() {
        return config;
    }

    public List<RigidBody> getBodies() {
        List<RigidBody> allBodies = new ArrayList<>();
        allBodies.addAll(dynamicBodies);
        allBodies.addAll(staticBodies);
        return allBodies;
    }

    public List<DynamicBody> getDynamicBodies() {
        return dynamicBodies;
    }

    public List<StaticBody> getStaticBodies() {
        return staticBodies;
    }

    public DynamicCollisionConstraint getDynamicCollisionConstraint() {
        return dynamicCollisionConstraint;
    }
}
