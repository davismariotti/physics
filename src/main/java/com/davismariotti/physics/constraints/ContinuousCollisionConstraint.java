package com.davismariotti.physics.constraints;

import com.davismariotti.physics.collision.CollisionDetector;
import com.davismariotti.physics.collision.CollisionResult;
import com.davismariotti.physics.collision.TimeOfImpact;
import com.davismariotti.physics.collision.TOISolver;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.DynamicBody;
import com.davismariotti.physics.sprites.RigidBody;
import com.davismariotti.physics.sprites.StaticBody;

import java.util.List;

/**
 * Continuous collision constraint using Time of Impact (TOI) detection
 * Handles collisions between dynamic bodies and static bodies (e.g., balls and ground)
 * Uses analytical TOI solver to prevent tunneling and energy gain bugs
 */
public class ContinuousCollisionConstraint implements Constraint {
    private final List<StaticBody> staticBodies;
    private final Vector gravity;
    private static final int MAX_RECURSION_DEPTH = 4;
    private static final double TIME_EPSILON = 1e-6;

    public ContinuousCollisionConstraint(List<StaticBody> staticBodies, Vector gravity) {
        this.staticBodies = staticBodies;
        this.gravity = gravity;
    }

    @Override
    public void apply(RigidBody body, double epsilon) {
        // Only applies to dynamic bodies
        if (!(body instanceof DynamicBody dynamic)) {
            return;
        }

        // Check for penetration using discrete detection
        StaticBody collidingStatic = null;
        CollisionResult discreteResult = null;

        for (StaticBody staticBody : staticBodies) {
            CollisionResult result = CollisionDetector.checkCollision(
                    dynamic.getCollider(),
                    staticBody.getCollider()
            );

            if (result.hasCollision()) {
                collidingStatic = staticBody;
                discreteResult = result;
                break;  // Handle one collision at a time
            }
        }

        // If penetrating, compute TOI and resolve
        if (collidingStatic != null) {
            handleCollisionWithTOI(dynamic, collidingStatic, discreteResult, epsilon, 0);
        }
    }

    /**
     * Handle collision using TOI - rewind to exact impact moment, apply impulse, integrate forward
     */
    private void handleCollisionWithTOI(DynamicBody dynamic, StaticBody staticBody,
                                        CollisionResult discreteResult, double substepDelta, int recursionDepth) {
        // Prevent infinite recursion
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            // Fallback to discrete correction
            fallbackDiscreteCorrection(dynamic, discreteResult);
            return;
        }

        // Compute TOI
        TimeOfImpact toi = TOISolver.computeTOI(dynamic, staticBody, substepDelta, gravity);

        if (!toi.hasCollision() || toi.t() < TIME_EPSILON) {
            // No TOI found (already deeply penetrating) or TOI too small
            // Fallback to discrete position correction
            fallbackDiscreteCorrection(dynamic, discreteResult);
            return;
        }

        // Rewind to exact TOI moment
        dynamic.setPosition(toi.position());
        dynamic.setVelocity(toi.velocity());

        // Apply impulse at TOI
        double restitution = Math.min(dynamic.getCoefficientOfRestitution(), staticBody.getCoefficientOfRestitution());
        applyImpulse(dynamic, toi.normal(), restitution);

        // Integrate forward for remaining time
        double remainingTime = substepDelta * (1.0 - toi.t());
        if (remainingTime > TIME_EPSILON) {
            integrateForward(dynamic, remainingTime, recursionDepth + 1);
        }
    }

    /**
     * Integrate body forward from current state for given time
     * Recursively checks for new collisions during integration
     */
    private void integrateForward(DynamicBody dynamic, double remainingTime, int recursionDepth) {
        if (remainingTime <= TIME_EPSILON || recursionDepth >= MAX_RECURSION_DEPTH) {
            return;
        }

        // Store state before integration
        Vector posBefore = dynamic.getPosition();
        Vector velBefore = dynamic.getVelocity();
        dynamic.setPreviousPosition(posBefore);
        dynamic.setPreviousVelocity(velBefore);

        // Integrate forward
        Vector acceleration = gravity;
        Vector newPos = new Vector(
                posBefore.x() + velBefore.x() * remainingTime + 0.5 * acceleration.x() * remainingTime * remainingTime,
                posBefore.y() + velBefore.y() * remainingTime + 0.5 * acceleration.y() * remainingTime * remainingTime
        );
        Vector newVel = new Vector(
                velBefore.x() + acceleration.x() * remainingTime,
                velBefore.y() + acceleration.y() * remainingTime
        );

        dynamic.setPosition(newPos);
        dynamic.setVelocity(newVel);

        // Check for new collision during this integration
        for (StaticBody staticBody : staticBodies) {
            CollisionResult result = CollisionDetector.checkCollision(
                    dynamic.getCollider(),
                    staticBody.getCollider()
            );

            if (result.hasCollision()) {
                // Found new collision - handle it recursively
                handleCollisionWithTOI(dynamic, staticBody, result, remainingTime, recursionDepth);
                return;  // Handled recursively
            }
        }
    }

    /**
     * Apply impulse to dynamic body for collision with static body
     * Reflects velocity along normal with restitution
     */
    private void applyImpulse(DynamicBody dynamic, Vector normal, double restitution) {
        Vector velocity = dynamic.getVelocity();

        // Calculate velocity along normal
        double velAlongNormal = velocity.x() * normal.x() + velocity.y() * normal.y();

        // Only apply impulse if moving into surface
        if (velAlongNormal < 0) {
            // Reflect velocity
            Vector reflection = new Vector(
                    velocity.x() - 2 * velAlongNormal * normal.x(),
                    velocity.y() - 2 * velAlongNormal * normal.y()
            );

            // Apply restitution
            reflection = new Vector(
                    reflection.x() * restitution,
                    reflection.y() * restitution
            );

            dynamic.setVelocity(reflection);
        }
    }

    /**
     * Fallback to discrete position correction when TOI fails
     * Used for edge cases like already deeply penetrating
     */
    private void fallbackDiscreteCorrection(DynamicBody dynamic, CollisionResult collision) {
        Vector normal = collision.normal();
        double penetration = collision.penetrationDepth();
        Vector velocity = dynamic.getVelocity();

        // Calculate velocity along normal
        double velAlongNormal = velocity.x() * normal.x() + velocity.y() * normal.y();

        // Only correct if moving into surface
        if (velAlongNormal < 0) {
            // Correct position
            Vector correction = new Vector(
                    normal.x() * penetration,
                    normal.y() * penetration
            );
            dynamic.setPosition(dynamic.getPosition().add(correction));

            // Reflect velocity
            Vector reflection = new Vector(
                    velocity.x() - 2 * velAlongNormal * normal.x(),
                    velocity.y() - 2 * velAlongNormal * normal.y()
            );

            // Apply restitution
            double restitution = dynamic.getCoefficientOfRestitution();
            reflection = new Vector(
                    reflection.x() * restitution,
                    reflection.y() * restitution
            );

            dynamic.setVelocity(reflection);
        } else if (penetration > 0) {
            // Penetrating but moving away - just correct position without changing velocity
            Vector correction = new Vector(
                    normal.x() * penetration,
                    normal.y() * penetration
            );
            dynamic.setPosition(dynamic.getPosition().add(correction));
        }
    }
}
