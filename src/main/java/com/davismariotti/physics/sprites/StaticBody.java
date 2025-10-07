package com.davismariotti.physics.sprites;

import com.davismariotti.physics.kinematics.Vector;

/**
 * Abstract base class for static rigid bodies that don't move
 * No velocity, forces, or physics integration
 */
public abstract non-sealed class StaticBody implements RigidBody {
    private final Vector position;
    private final MaterialProperties material;

    public StaticBody(Vector position, MaterialProperties material) {
        this.position = position;
        this.material = material;
    }

    // Legacy constructor for backward compatibility
    public StaticBody(Vector position, double coefficientOfRestitution) {
        this(position, new MaterialProperties(coefficientOfRestitution, 0.0, 0.5, 0.35));
    }

    @Override
    public Vector getPosition() {
        return position;
    }

    @Override
    public double getCoefficientOfRestitution() {
        return material.coefficientOfRestitution();
    }

    public MaterialProperties getMaterial() {
        return material;
    }
}
