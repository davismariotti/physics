package com.davismariotti.physics.constraints;

import com.davismariotti.physics.collision.CollisionDetector;
import com.davismariotti.physics.collision.CollisionResult;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.RigidBody;

import java.util.List;

/**
 * Constraint that handles collisions between dynamic bodies and static bodies
 */
public class CollisionConstraint implements Constraint {
    private final List<RigidBody> staticBodies;

    public CollisionConstraint(List<RigidBody> staticBodies) {
        this.staticBodies = staticBodies;
    }

    @Override
    public void apply(RigidBody body, double epsilon) {
        // Skip static bodies
        if (body.isStatic()) {
            return;
        }

        // Check collision against all static bodies
        for (RigidBody staticBody : staticBodies) {
            if (!staticBody.isStatic()) {
                continue;  // Only collide with static bodies
            }

            CollisionResult result = CollisionDetector.checkCollision(
                    body.getCollider(),
                    staticBody.getCollider()
            );

            if (result.hasCollision()) {
                resolveCollision(body, result);
            }
        }
    }

    /**
     * Resolve a collision by correcting position and reflecting velocity
     */
    private void resolveCollision(RigidBody body, CollisionResult collision) {
        Vector normal = collision.normal();
        double penetration = collision.penetrationDepth();

        // Correct position: push body out of penetration
        Vector correction = new Vector(
                normal.x() * penetration,
                normal.y() * penetration
        );
        body.setPosition(body.getPosition().add(correction));

        // Reflect velocity along collision normal
        Vector velocity = body.getVelocity();
        double dotProduct = velocity.x() * normal.x() + velocity.y() * normal.y();

        // Only reflect if moving into the surface
        if (dotProduct < 0) {
            Vector reflection = new Vector(
                    velocity.x() - 2 * dotProduct * normal.x(),
                    velocity.y() - 2 * dotProduct * normal.y()
            );

            // Apply coefficient of restitution
            reflection = new Vector(
                    reflection.x() * body.getCoefficientOfRestitution(),
                    reflection.y() * body.getCoefficientOfRestitution()
            );

            body.setVelocity(reflection);
        }
    }
}
