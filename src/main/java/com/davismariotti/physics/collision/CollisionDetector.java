package com.davismariotti.physics.collision;

import com.davismariotti.physics.kinematics.Vector;

/**
 * Utility class for detecting collisions between different collider types
 */
public class CollisionDetector {

    /**
     * Detect collision between two colliders
     * Dispatches to specific collision detection methods based on collider types
     */
    public static CollisionResult checkCollision(Collider a, Collider b) {
        // Circle vs AABB
        if (a instanceof CircleCollider circle && b instanceof AABBCollider aabb) {
            return circleVsAABB(circle, aabb);
        }
        // AABB vs Circle (reverse)
        if (a instanceof AABBCollider aabb && b instanceof CircleCollider circle) {
            CollisionResult result = circleVsAABB(circle, aabb);
            // Flip normal direction when reversing
            if (result.hasCollision()) {
                return CollisionResult.collision(
                        new Vector(-result.normal().x(), -result.normal().y()),
                        result.penetrationDepth()
                );
            }
            return result;
        }
        // Circle vs Circle
        if (a instanceof CircleCollider c1 && b instanceof CircleCollider c2) {
            return circleVsCircle(c1, c2);
        }
        // AABB vs AABB
        if (a instanceof AABBCollider a1 && b instanceof AABBCollider a2) {
            return aabbVsAABB(a1, a2);
        }

        // Unknown collision pair
        return CollisionResult.NO_COLLISION;
    }

    /**
     * Detect collision between a circle and an AABB
     */
    public static CollisionResult circleVsAABB(CircleCollider circle, AABBCollider aabb) {
        Vector circleCenter = circle.center();
        double radius = circle.radius();

        Vector aabbMin = aabb.getMin();
        Vector aabbMax = aabb.getMax();

        // Find the closest point on the AABB to the circle center
        double closestX = Math.max(aabbMin.x(), Math.min(circleCenter.x(), aabbMax.x()));
        double closestY = Math.max(aabbMin.y(), Math.min(circleCenter.y(), aabbMax.y()));
        Vector closestPoint = new Vector(closestX, closestY);

        // Calculate distance from circle center to closest point
        double dx = circleCenter.x() - closestPoint.x();
        double dy = circleCenter.y() - closestPoint.y();
        double distanceSquared = dx * dx + dy * dy;

        // Check if collision occurred
        if (distanceSquared < radius * radius) {
            double distance = Math.sqrt(distanceSquared);
            double penetration = radius - distance;

            // Calculate collision normal
            Vector normal;
            if (distance > 0.0001) {  // Avoid division by zero
                // Normal points from closest point toward circle center
                normal = new Vector(dx / distance, dy / distance);
            } else {
                // Circle center is inside AABB, use direction to nearest face
                double leftDist = Math.abs(circleCenter.x() - aabbMin.x());
                double rightDist = Math.abs(circleCenter.x() - aabbMax.x());
                double bottomDist = Math.abs(circleCenter.y() - aabbMin.y());
                double topDist = Math.abs(circleCenter.y() - aabbMax.y());

                double minDist = Math.min(Math.min(leftDist, rightDist), Math.min(bottomDist, topDist));

                if (minDist == bottomDist) {
                    normal = new Vector(0, -1);  // Bottom face
                } else if (minDist == topDist) {
                    normal = new Vector(0, 1);   // Top face
                } else if (minDist == leftDist) {
                    normal = new Vector(-1, 0);  // Left face
                } else {
                    normal = new Vector(1, 0);   // Right face
                }
            }

            return CollisionResult.collision(normal, penetration);
        }

        return CollisionResult.NO_COLLISION;
    }

    /**
     * Detect collision between two circles
     */
    public static CollisionResult circleVsCircle(CircleCollider c1, CircleCollider c2) {
        double dx = c2.center().x() - c1.center().x();
        double dy = c2.center().y() - c1.center().y();
        double distanceSquared = dx * dx + dy * dy;
        double radiusSum = c1.radius() + c2.radius();

        if (distanceSquared < radiusSum * radiusSum) {
            double distance = Math.sqrt(distanceSquared);
            double penetration = radiusSum - distance;

            Vector normal;
            if (distance > 0.0001) {
                normal = new Vector(dx / distance, dy / distance);
            } else {
                // Circles at same position, arbitrary normal
                normal = new Vector(1, 0);
            }

            return CollisionResult.collision(normal, penetration);
        }

        return CollisionResult.NO_COLLISION;
    }

    /**
     * Detect collision between two AABBs
     */
    public static CollisionResult aabbVsAABB(AABBCollider a1, AABBCollider a2) {
        Vector min1 = a1.getMin();
        Vector max1 = a1.getMax();
        Vector min2 = a2.getMin();
        Vector max2 = a2.getMax();

        // Check for overlap
        if (max1.x() < min2.x() || min1.x() > max2.x() ||
            max1.y() < min2.y() || min1.y() > max2.y()) {
            return CollisionResult.NO_COLLISION;
        }

        // Calculate overlap on each axis
        double overlapX = Math.min(max1.x() - min2.x(), max2.x() - min1.x());
        double overlapY = Math.min(max1.y() - min2.y(), max2.y() - min1.y());

        // Use smallest overlap as penetration depth
        Vector normal;
        double penetration;

        if (overlapX < overlapY) {
            penetration = overlapX;
            normal = a1.center().x() < a2.center().x() ? new Vector(-1, 0) : new Vector(1, 0);
        } else {
            penetration = overlapY;
            normal = a1.center().y() < a2.center().y() ? new Vector(0, -1) : new Vector(0, 1);
        }

        return CollisionResult.collision(normal, penetration);
    }
}
