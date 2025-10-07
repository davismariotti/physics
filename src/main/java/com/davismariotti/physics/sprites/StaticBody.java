package com.davismariotti.physics.sprites;

import com.davismariotti.physics.kinematics.Vector;

/**
 * Abstract base class for static rigid bodies that don't move
 * No velocity, forces, or physics integration
 */
public abstract non-sealed class StaticBody implements RigidBody {
    private final Vector position;
    private final double coefficientOfRestitution;

    public StaticBody(Vector position, double coefficientOfRestitution) {
        this.position = position;
        this.coefficientOfRestitution = coefficientOfRestitution;
    }

    @Override
    public Vector getPosition() {
        return position;
    }

    @Override
    public double getCoefficientOfRestitution() {
        return coefficientOfRestitution;
    }
}
