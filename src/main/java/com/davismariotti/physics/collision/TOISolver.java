package com.davismariotti.physics.collision;

import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.DynamicBody;
import com.davismariotti.physics.sprites.StaticBody;

/**
 * Time of Impact (TOI) solver for continuous collision detection
 * Calculates the exact moment when two bodies first make contact during a timestep
 */
public class TOISolver {
    private static final double EPSILON = 1e-10;

    /**
     * Compute time of impact between a dynamic body and a static body
     * Uses analytical solution for circle-AABB collision
     *
     * @param dynamic The moving dynamic body
     * @param staticBody The stationary static body
     * @param substepDelta The timestep duration
     * @param gravity The gravity vector
     * @return TimeOfImpact containing collision time and state, or NO_COLLISION if no hit
     */
    public static TimeOfImpact computeTOI(DynamicBody dynamic, StaticBody staticBody,
                                          double substepDelta, Vector gravity) {
        Collider dynamicCollider = dynamic.getCollider();
        Collider staticCollider = staticBody.getCollider();

        // Currently only support circle-AABB (ball-ground)
        if (dynamicCollider instanceof CircleCollider circle && staticCollider instanceof AABBCollider aabb) {
            return computeCircleAABBTOI(circle, dynamic.getPreviousPosition(), dynamic.getPreviousVelocity(),
                    dynamic.getPosition(), dynamic.getVelocity(), aabb, substepDelta, gravity);
        }

        // Unsupported collision pair - no TOI available
        return TimeOfImpact.NO_COLLISION;
    }

    /**
     * Compute TOI for circle-AABB collision using quadratic solver
     * Assumes AABB represents horizontal ground at bottom
     */
    private static TimeOfImpact computeCircleAABBTOI(CircleCollider circle, Vector prevPos, Vector prevVel,
                                                      Vector currPos, Vector currVel,
                                                      AABBCollider aabb, double substepDelta, Vector gravity) {
        double radius = circle.radius();
        Vector aabbMin = aabb.getMin();
        Vector aabbMax = aabb.getMax();

        // Find the closest point on AABB to current circle position
        double closestX = Math.max(aabbMin.x(), Math.min(currPos.x(), aabbMax.x()));
        double closestY = Math.max(aabbMin.y(), Math.min(currPos.y(), aabbMax.y()));

        // Determine which face we're colliding with
        // For ground (horizontal AABB), we primarily care about top face collision
        boolean isTopFace = closestY == aabbMax.y() && currPos.y() > aabbMax.y();

        if (isTopFace) {
            // Ball falling onto horizontal ground - use analytical solver
            // Solve: y(t) = y0 + v0*t + 0.5*a*t^2 = groundTop + radius
            double y0 = prevPos.y();
            double v0 = prevVel.y();
            double a = gravity.y();
            double targetY = aabbMax.y() + radius;

            // Quadratic equation: 0.5*a*t^2 + v0*t + (y0 - targetY) = 0
            double A = 0.5 * a;
            double B = v0;
            double C = y0 - targetY;

            // If no acceleration, linear motion
            if (Math.abs(A) < EPSILON) {
                if (Math.abs(B) < EPSILON) {
                    // No motion in Y direction
                    return TimeOfImpact.NO_COLLISION;
                }
                double t = -C / B;
                if (t >= 0 && t <= substepDelta) {
                    Vector posAtTOI = calculatePositionAtTime(prevPos, prevVel, gravity, t);
                    Vector velAtTOI = calculateVelocityAtTime(prevVel, gravity, t);
                    Vector normal = new Vector(0, 1); // Ground normal points up
                    return TimeOfImpact.collision(t / substepDelta, posAtTOI, velAtTOI, normal);
                }
                return TimeOfImpact.NO_COLLISION;
            }

            // Solve quadratic equation
            double discriminant = B * B - 4 * A * C;

            if (discriminant < 0) {
                // No real solutions - no collision
                return TimeOfImpact.NO_COLLISION;
            }

            double sqrtDisc = Math.sqrt(discriminant);
            double t1 = (-B - sqrtDisc) / (2 * A);
            double t2 = (-B + sqrtDisc) / (2 * A);

            // We want the earliest positive time within our timestep
            double t = -1;
            if (t1 >= 0 && t1 <= substepDelta) {
                t = t1;
            } else if (t2 >= 0 && t2 <= substepDelta) {
                t = t2;
            }

            if (t >= 0) {
                Vector posAtTOI = calculatePositionAtTime(prevPos, prevVel, gravity, t);
                Vector velAtTOI = calculateVelocityAtTime(prevVel, gravity, t);

                // Verify ball is within horizontal bounds of AABB
                if (posAtTOI.x() >= aabbMin.x() - radius && posAtTOI.x() <= aabbMax.x() + radius) {
                    Vector normal = new Vector(0, 1); // Ground normal points up
                    return TimeOfImpact.collision(t / substepDelta, posAtTOI, velAtTOI, normal);
                }
            }
        }

        // TODO: Handle other faces (side collisions, corner collisions)
        // For now, return no collision for non-top-face cases
        return TimeOfImpact.NO_COLLISION;
    }

    /**
     * Calculate position at time t given initial position, velocity, and constant acceleration
     */
    private static Vector calculatePositionAtTime(Vector pos0, Vector vel0, Vector accel, double t) {
        return new Vector(
                pos0.x() + vel0.x() * t + 0.5 * accel.x() * t * t,
                pos0.y() + vel0.y() * t + 0.5 * accel.y() * t * t
        );
    }

    /**
     * Calculate velocity at time t given initial velocity and constant acceleration
     */
    private static Vector calculateVelocityAtTime(Vector vel0, Vector accel, double t) {
        return new Vector(
                vel0.x() + accel.x() * t,
                vel0.y() + accel.y() * t
        );
    }

    /**
     * Compute TOI between two dynamic bodies (ball vs ball)
     * Uses relative frame of reference
     */
    public static TimeOfImpact computeTOI(DynamicBody bodyA, DynamicBody bodyB,
                                          double substepDelta, Vector gravity) {
        Collider colliderA = bodyA.getCollider();
        Collider colliderB = bodyB.getCollider();

        // Currently only support circle-circle (ball vs ball)
        if (colliderA instanceof CircleCollider circleA && colliderB instanceof CircleCollider circleB) {
            return computeCircleCircleTOI(circleA, bodyA.getPreviousPosition(), bodyA.getPreviousVelocity(),
                    circleB, bodyB.getPreviousPosition(), bodyB.getPreviousVelocity(),
                    substepDelta, gravity);
        }

        return TimeOfImpact.NO_COLLISION;
    }

    /**
     * Compute TOI for circle-circle collision using relative frame
     * Transform to relative coordinates and solve as circle vs point
     */
    private static TimeOfImpact computeCircleCircleTOI(CircleCollider circleA, Vector posA0, Vector velA0,
                                                        CircleCollider circleB, Vector posB0, Vector velB0,
                                                        double substepDelta, Vector gravity) {
        double radiusA = circleA.radius();
        double radiusB = circleB.radius();
        double combinedRadius = radiusA + radiusB;

        // Relative position and velocity (B relative to A)
        Vector relPos = new Vector(posB0.x() - posA0.x(), posB0.y() - posA0.y());
        Vector relVel = new Vector(velB0.x() - velA0.x(), velB0.y() - velA0.y());

        // Both bodies have same gravity, so relative acceleration is zero
        Vector relAccel = Vector.ZERO;

        // Solve: |relPos + relVel*t + 0.5*relAccel*t^2| = combinedRadius
        // This expands to quadratic: a*t^2 + b*t + c = 0

        double a = 0.5 * (relAccel.x() * relAccel.x() + relAccel.y() * relAccel.y());
        double b = relVel.x() * relVel.x() + relVel.y() * relVel.y()
                + relAccel.x() * relPos.x() + relAccel.y() * relPos.y();
        double c = relPos.x() * relPos.x() + relPos.y() * relPos.y() - combinedRadius * combinedRadius
                + relPos.x() * relVel.x() + relPos.y() * relVel.y();

        // Simplified quadratic (since relAccel is zero in our case)
        // a*t^2 + 2*(relVel·relPos)*t + (|relPos|^2 - r^2) = 0
        a = relVel.x() * relVel.x() + relVel.y() * relVel.y();
        b = 2 * (relVel.x() * relPos.x() + relVel.y() * relPos.y());
        c = relPos.x() * relPos.x() + relPos.y() * relPos.y() - combinedRadius * combinedRadius;

        if (Math.abs(a) < EPSILON) {
            // Linear case
            if (Math.abs(b) < EPSILON) {
                return TimeOfImpact.NO_COLLISION;
            }
            double t = -c / b;
            if (t >= 0 && t <= substepDelta) {
                Vector posAtTOI = calculatePositionAtTime(posA0, velA0, gravity, t);
                Vector velAtTOI = calculateVelocityAtTime(velA0, gravity, t);
                Vector posBAtTOI = calculatePositionAtTime(posB0, velB0, gravity, t);
                Vector normal = new Vector(
                        (posBAtTOI.x() - posAtTOI.x()) / combinedRadius,
                        (posBAtTOI.y() - posAtTOI.y()) / combinedRadius
                );
                return TimeOfImpact.collision(t / substepDelta, posAtTOI, velAtTOI, normal);
            }
            return TimeOfImpact.NO_COLLISION;
        }

        // Quadratic case
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return TimeOfImpact.NO_COLLISION;
        }

        double sqrtDisc = Math.sqrt(discriminant);
        double t1 = (-b - sqrtDisc) / (2 * a);
        double t2 = (-b + sqrtDisc) / (2 * a);

        // Take earliest positive time
        double t = -1;
        if (t1 >= 0 && t1 <= substepDelta) {
            t = t1;
        } else if (t2 >= 0 && t2 <= substepDelta) {
            t = t2;
        }

        if (t >= 0) {
            Vector posAtTOI = calculatePositionAtTime(posA0, velA0, gravity, t);
            Vector velAtTOI = calculateVelocityAtTime(velA0, gravity, t);
            Vector posBAtTOI = calculatePositionAtTime(posB0, velB0, gravity, t);

            // Normal points from A to B
            double dx = posBAtTOI.x() - posAtTOI.x();
            double dy = posBAtTOI.y() - posAtTOI.y();
            double dist = Math.sqrt(dx * dx + dy * dy);
            Vector normal = new Vector(dx / dist, dy / dist);

            return TimeOfImpact.collision(t / substepDelta, posAtTOI, velAtTOI, normal);
        }

        return TimeOfImpact.NO_COLLISION;
    }
}
