package com.davismariotti.physics.constraints;

import com.davismariotti.physics.collision.CollisionDetector;
import com.davismariotti.physics.collision.CollisionResult;
import com.davismariotti.physics.collision.TimeOfImpact;
import com.davismariotti.physics.collision.TOISolver;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.DynamicBody;
import com.davismariotti.physics.sprites.RigidBody;

import java.util.List;

/**
 * Continuous collision constraint for dynamic-dynamic collisions (ball vs ball)
 * Uses Time of Impact (TOI) detection and two-body momentum exchange
 * Currently suppressed - can be enabled via PhysicsConfig
 */
public class DynamicCollisionConstraint implements Constraint {
    private final List<DynamicBody> dynamicBodies;
    private final Vector gravity;
    private static final int MAX_RECURSION_DEPTH = 4;
    private static final double TIME_EPSILON = 1e-6;

    public DynamicCollisionConstraint(List<DynamicBody> dynamicBodies, Vector gravity) {
        this.dynamicBodies = dynamicBodies;
        this.gravity = gravity;
    }

    @Override
    public void apply(RigidBody body, double epsilon) {
        // This constraint is applied differently - see applyAll() below
        // Not used for dynamic-dynamic collisions
    }

    /**
     * Apply dynamic collision detection to all body pairs
     * Should be called once per substep, not per body
     */
    public void applyAll(double substepDelta) {
        // Check all pairs of dynamic bodies
        for (int i = 0; i < dynamicBodies.size(); i++) {
            for (int j = i + 1; j < dynamicBodies.size(); j++) {
                DynamicBody bodyA = dynamicBodies.get(i);
                DynamicBody bodyB = dynamicBodies.get(j);

                // Check for penetration
                CollisionResult result = CollisionDetector.checkCollision(
                        bodyA.getCollider(),
                        bodyB.getCollider()
                );

                if (result.hasCollision()) {
                    // Check if velocities are separating - if so, skip resolution
                    Vector velA = bodyA.getVelocity();
                    Vector velB = bodyB.getVelocity();
                    Vector relVel = new Vector(velB.x() - velA.x(), velB.y() - velA.y());
                    Vector normal = result.normal();
                    double velAlongNormal = relVel.x() * normal.x() + relVel.y() * normal.y();

                    // Only resolve if approaching (velAlongNormal < 0)
                    if (velAlongNormal < 0) {
                        handleDynamicCollision(bodyA, bodyB, result, substepDelta, 0);
                    }
                }
            }
        }
    }

    /**
     * Handle collision between two dynamic bodies using TOI
     */
    private void handleDynamicCollision(DynamicBody bodyA, DynamicBody bodyB,
                                        CollisionResult discreteResult, double substepDelta, int recursionDepth) {
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            // Fallback to discrete correction
            fallbackDiscreteCorrection(bodyA, bodyB, discreteResult);
            return;
        }

        // Compute TOI
        TimeOfImpact toi = TOISolver.computeTOI(bodyA, bodyB, substepDelta, gravity);

        if (!toi.hasCollision() || toi.t() < TIME_EPSILON) {
            // Fallback to discrete correction
            fallbackDiscreteCorrection(bodyA, bodyB, discreteResult);
            return;
        }

        // Rewind both bodies to TOI
        Vector posA = calculatePositionAtTime(bodyA.getPreviousPosition(), bodyA.getPreviousVelocity(), gravity, toi.t() * substepDelta);
        Vector velA = calculateVelocityAtTime(bodyA.getPreviousVelocity(), gravity, toi.t() * substepDelta);
        Vector posB = calculatePositionAtTime(bodyB.getPreviousPosition(), bodyB.getPreviousVelocity(), gravity, toi.t() * substepDelta);
        Vector velB = calculateVelocityAtTime(bodyB.getPreviousVelocity(), gravity, toi.t() * substepDelta);

        bodyA.setPosition(posA);
        bodyA.setVelocity(velA);
        bodyB.setPosition(posB);
        bodyB.setVelocity(velB);

        // Apply two-body impulse
        double restitution = Math.min(bodyA.getCoefficientOfRestitution(), bodyB.getCoefficientOfRestitution());
        applyDynamicImpulse(bodyA, bodyB, toi.normal(), restitution);

        // Integrate both bodies forward
        double remainingTime = substepDelta * (1.0 - toi.t());
        if (remainingTime > TIME_EPSILON) {
            integrateDynamicBodiesForward(bodyA, bodyB, remainingTime);
        }
    }

    /**
     * Apply two-body impulse for momentum exchange
     */
    private void applyDynamicImpulse(DynamicBody bodyA, DynamicBody bodyB, Vector normal, double restitution) {
        Vector velA = bodyA.getVelocity();
        Vector velB = bodyB.getVelocity();

        // Relative velocity
        Vector relVel = new Vector(velB.x() - velA.x(), velB.y() - velA.y());
        double velAlongNormal = relVel.x() * normal.x() + relVel.y() * normal.y();

        // Don't resolve if velocities are separating
        if (velAlongNormal > 0) {
            return;
        }

        // Calculate impulse scalar
        double massA = bodyA.getMass();
        double massB = bodyB.getMass();
        double impulseScalar = -(1 + restitution) * velAlongNormal;
        impulseScalar /= (1 / massA + 1 / massB);

        // Apply impulse
        Vector impulse = new Vector(normal.x() * impulseScalar, normal.y() * impulseScalar);

        // Impulse pushes A backward (opposite to normal) and B forward (along normal)
        Vector newVelA = new Vector(
                velA.x() - impulse.x() / massA,
                velA.y() - impulse.y() / massA
        );
        Vector newVelB = new Vector(
                velB.x() + impulse.x() / massB,
                velB.y() + impulse.y() / massB
        );

        bodyA.setVelocity(newVelA);
        bodyB.setVelocity(newVelB);
    }

    /**
     * Integrate both bodies forward and check for new collisions
     */
    private void integrateDynamicBodiesForward(DynamicBody bodyA, DynamicBody bodyB, double remainingTime) {
        // Simple forward integration - doesn't handle recursive collisions yet
        Vector posA = calculatePositionAtTime(bodyA.getPosition(), bodyA.getVelocity(), gravity, remainingTime);
        Vector velA = calculateVelocityAtTime(bodyA.getVelocity(), gravity, remainingTime);
        Vector posB = calculatePositionAtTime(bodyB.getPosition(), bodyB.getVelocity(), gravity, remainingTime);
        Vector velB = calculateVelocityAtTime(bodyB.getVelocity(), gravity, remainingTime);

        bodyA.setPosition(posA);
        bodyA.setVelocity(velA);
        bodyB.setPosition(posB);
        bodyB.setVelocity(velB);
    }

    /**
     * Fallback to discrete correction for edge cases
     */
    private void fallbackDiscreteCorrection(DynamicBody bodyA, DynamicBody bodyB, CollisionResult collision) {
        Vector normal = collision.normal();
        double penetration = collision.penetrationDepth();

        // Correct positions
        Vector correction = new Vector(normal.x() * penetration * 0.5, normal.y() * penetration * 0.5);
        bodyA.setPosition(bodyA.getPosition().add(correction.multiply(-1)));
        bodyB.setPosition(bodyB.getPosition().add(correction));

        // Apply impulse
        double restitution = Math.min(bodyA.getCoefficientOfRestitution(), bodyB.getCoefficientOfRestitution());
        applyDynamicImpulse(bodyA, bodyB, normal, restitution);
    }

    private Vector calculatePositionAtTime(Vector pos0, Vector vel0, Vector accel, double t) {
        return new Vector(
                pos0.x() + vel0.x() * t + 0.5 * accel.x() * t * t,
                pos0.y() + vel0.y() * t + 0.5 * accel.y() * t * t
        );
    }

    private Vector calculateVelocityAtTime(Vector vel0, Vector accel, double t) {
        return new Vector(
                vel0.x() + accel.x() * t,
                vel0.y() + accel.y() * t
        );
    }
}
