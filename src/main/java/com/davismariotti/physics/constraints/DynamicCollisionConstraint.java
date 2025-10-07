package com.davismariotti.physics.constraints;

import com.davismariotti.physics.collision.CollisionDetector;
import com.davismariotti.physics.collision.CollisionResult;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.RigidBody;

import java.util.*;


/**
 * Constraint that handles collisions between dynamic bodies
 * Implements momentum exchange and proper two-body collision resolution
 */
public class DynamicCollisionConstraint implements Constraint {
    private final List<RigidBody> allBodies;
    private final List<RigidBody> dynamicBodies;
    private final CollisionMetrics metrics;

    public DynamicCollisionConstraint(List<RigidBody> allBodies) {
        this.allBodies = allBodies;
        this.dynamicBodies = new ArrayList<>();
        this.metrics = new CollisionMetrics();
    }

    @Override
    public void apply(RigidBody body, double epsilon) {
        // This constraint is handled differently - see applyAll() below
        // Individual body application not used for dynamic-dynamic collisions
    }

    /**
     * Apply dynamic collision detection to all body pairs
     * Called once per physics step, not per body
     *
     * This method is designed to be extensible - getPotentialCollisionPairs()
     * can be replaced with spatial hash optimization in the future
     */
    public void applyAll() {
        // Reset metrics for this frame
        metrics.reset();

        // Update dynamic bodies list
        updateDynamicBodies();

        // Detect clusters (3+ balls in contact)
        detectClusters();

        // Use iterative solver to handle stacked/clustered objects
        // Resolving one collision can affect others, so we need multiple passes
        final int SOLVER_ITERATIONS = 3;

        for (int iteration = 0; iteration < SOLVER_ITERATIONS; iteration++) {
            // Get pairs to check (currently all pairs, but can be optimized with spatial hash)
            List<BodyPair> pairs = getPotentialCollisionPairs();

            // Check and resolve collisions for each pair
            for (BodyPair pair : pairs) {
                CollisionResult result = CollisionDetector.checkCollision(
                        pair.bodyA.getCollider(),
                        pair.bodyB.getCollider()
                );

                if (result.hasCollision()) {
                    resolveDynamicCollision(pair.bodyA, pair.bodyB, result);
                }
            }
        }
    }

    /**
     * Get all pairs of dynamic bodies that should be checked for collision
     *
     * EXTENSIBILITY: This method can be replaced with spatial hash optimization
     * to return only nearby pairs instead of all pairs
     *
     * @return list of body pairs to check
     */
    protected List<BodyPair> getPotentialCollisionPairs() {
        List<BodyPair> pairs = new ArrayList<>();

        // Check each pair once (avoid duplicate checks)
        for (int i = 0; i < dynamicBodies.size(); i++) {
            for (int j = i + 1; j < dynamicBodies.size(); j++) {
                pairs.add(new BodyPair(dynamicBodies.get(i), dynamicBodies.get(j)));
            }
        }

        return pairs;
    }

    /**
     * Resolve collision between two dynamic bodies
     * Handles momentum exchange and position correction for both bodies
     */
    private void resolveDynamicCollision(RigidBody bodyA, RigidBody bodyB, CollisionResult collision) {
        Vector normal = collision.normal();
        double penetration = collision.penetrationDepth();

        // Correct positions: split penetration between both bodies
        // Add small slop allowance - don't correct tiny penetrations
        final double PENETRATION_SLOP = 0.01;
        double correctionDepth = Math.max(penetration - PENETRATION_SLOP, 0.0);

        if (correctionDepth > 0) {
            Vector correction = new Vector(
                    normal.x() * correctionDepth * 0.5,
                    normal.y() * correctionDepth * 0.5
            );
            bodyA.setPosition(bodyA.getPosition().add(correction));
            bodyB.setPosition(bodyB.getPosition().add(correction.multiply(-1)));
        }

        // Calculate relative velocity
        Vector vA = bodyA.getVelocity();
        Vector vB = bodyB.getVelocity();
        Vector relativeVelocity = new Vector(
                vA.x() - vB.x(),
                vA.y() - vB.y()
        );

        // Calculate velocity along collision normal
        double velocityAlongNormal = relativeVelocity.x() * normal.x() + relativeVelocity.y() * normal.y();

        // Only apply impulse if bodies are approaching with significant velocity
        // This prevents jittering when balls are resting against each other
        final double RESTING_VELOCITY_THRESHOLD = 0.5;
        boolean appliedImpulse = false;
        if (velocityAlongNormal > -RESTING_VELOCITY_THRESHOLD) {
            // Resting contact - don't apply impulse, position correction is enough
            metrics.recordCollision(penetration, false);
            return;
        } else {
            appliedImpulse = true;
        }

        // Calculate coefficient of restitution (use minimum of both bodies)
        double restitution = Math.min(bodyA.getCoefficientOfRestitution(), bodyB.getCoefficientOfRestitution());

        // Calculate impulse scalar
        // For equal masses, this simplifies to just swapping velocity components along normal
        double massA = bodyA.getMass();
        double massB = bodyB.getMass();
        double impulseScalar = -(1 + restitution) * velocityAlongNormal;
        impulseScalar /= (1 / massA + 1 / massB);

        // Apply impulse to both bodies
        Vector impulse = new Vector(
                normal.x() * impulseScalar,
                normal.y() * impulseScalar
        );

        Vector newVelocityA = new Vector(
                vA.x() + impulse.x() / massA,
                vA.y() + impulse.y() / massA
        );
        Vector newVelocityB = new Vector(
                vB.x() - impulse.x() / massB,
                vB.y() - impulse.y() / massB
        );

        bodyA.setVelocity(newVelocityA);
        bodyB.setVelocity(newVelocityB);

        // Record metrics
        metrics.recordCollision(penetration, appliedImpulse);
    }

    /**
     * Detect clusters of balls in contact (3+ balls touching)
     * Uses union-find algorithm to group connected balls
     */
    private void detectClusters() {
        if (dynamicBodies.isEmpty()) {
            return;
        }

        // Build adjacency map: which balls are touching which
        Map<RigidBody, Set<RigidBody>> adjacency = new HashMap<>();
        for (RigidBody body : dynamicBodies) {
            adjacency.put(body, new HashSet<>());
        }

        // Check all pairs for contact
        List<BodyPair> pairs = getPotentialCollisionPairs();
        for (BodyPair pair : pairs) {
            CollisionResult result = CollisionDetector.checkCollision(
                    pair.bodyA.getCollider(),
                    pair.bodyB.getCollider()
            );
            if (result.hasCollision()) {
                adjacency.get(pair.bodyA).add(pair.bodyB);
                adjacency.get(pair.bodyB).add(pair.bodyA);
            }
        }

        // Find connected components (clusters) using DFS
        Set<RigidBody> visited = new HashSet<>();
        int maxClusterSize = 0;

        for (RigidBody body : dynamicBodies) {
            if (!visited.contains(body)) {
                int clusterSize = exploreCluster(body, adjacency, visited);
                if (clusterSize >= 3) {
                    maxClusterSize = Math.max(maxClusterSize, clusterSize);
                }
            }
        }

        metrics.recordClusteredBalls(maxClusterSize);
    }

    /**
     * DFS to find size of connected cluster
     */
    private int exploreCluster(RigidBody start, Map<RigidBody, Set<RigidBody>> adjacency, Set<RigidBody> visited) {
        if (visited.contains(start)) {
            return 0;
        }

        visited.add(start);
        int size = 1;

        for (RigidBody neighbor : adjacency.get(start)) {
            size += exploreCluster(neighbor, adjacency, visited);
        }

        return size;
    }

    /**
     * Update the cached list of dynamic bodies
     */
    private void updateDynamicBodies() {
        dynamicBodies.clear();
        for (RigidBody body : allBodies) {
            if (!body.isStatic()) {
                dynamicBodies.add(body);
            }
        }
    }

    /**
     * Get collision metrics for observability
     */
    public CollisionMetrics getMetrics() {
        return metrics;
    }

    /**
     * Simple record to hold a pair of bodies for collision checking
     */
    protected record BodyPair(RigidBody bodyA, RigidBody bodyB) {
    }
}
