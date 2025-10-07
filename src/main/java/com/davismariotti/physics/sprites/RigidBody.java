package com.davismariotti.physics.sprites;

import com.davismariotti.physics.collision.Collider;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.rendering.Camera;

import java.awt.*;

/**
 * Top-level interface for all rigid bodies in the physics simulation
 * Sealed to enforce type safety between dynamic and static bodies
 */
public sealed interface RigidBody permits DynamicBody, StaticBody {
    Vector getPosition();

    Collider getCollider();

    double getCoefficientOfRestitution();

    void draw(Graphics2D graphics, Camera camera);
}
