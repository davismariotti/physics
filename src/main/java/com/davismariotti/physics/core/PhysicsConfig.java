package com.davismariotti.physics.core;

import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.MaterialProperties;

public class PhysicsConfig {
    private Vector gravity;
    private double scale;
    private double gameSpeed;
    private MaterialProperties defaultMaterial;
    private int substeps;
    private double restingVelocityThreshold;  // Velocity below which restitution = 0

    public PhysicsConfig() {
        this.gravity = new Vector(0, -9.8);
        this.scale = 10.0;
        this.gameSpeed = 3.0;
        this.defaultMaterial = MaterialProperties.DEFAULT;
        this.substeps = 4;
        this.restingVelocityThreshold = 0.5;
    }

    public Vector getGravity() {
        return gravity;
    }

    public void setGravity(Vector gravity) {
        this.gravity = gravity;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(double gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    public MaterialProperties getDefaultMaterial() {
        return defaultMaterial;
    }

    public void setDefaultMaterial(MaterialProperties defaultMaterial) {
        this.defaultMaterial = defaultMaterial;
    }

    // Legacy methods for backward compatibility
    public double getCoefficientOfRestitution() {
        return defaultMaterial.coefficientOfRestitution();
    }

    public void setCoefficientOfRestitution(double coefficientOfRestitution) {
        this.defaultMaterial = new MaterialProperties(
                coefficientOfRestitution,
                defaultMaterial.dragCoefficient(),
                defaultMaterial.staticFriction(),
                defaultMaterial.dynamicFriction()
        );
    }

    public double getDragCoefficient() {
        return defaultMaterial.dragCoefficient();
    }

    public void setDragCoefficient(double dragCoefficient) {
        this.defaultMaterial = new MaterialProperties(
                defaultMaterial.coefficientOfRestitution(),
                dragCoefficient,
                defaultMaterial.staticFriction(),
                defaultMaterial.dynamicFriction()
        );
    }

    public int getSubsteps() {
        return substeps;
    }

    public void setSubsteps(int substeps) {
        this.substeps = substeps;
    }

    public double getRestingVelocityThreshold() {
        return restingVelocityThreshold;
    }

    public void setRestingVelocityThreshold(double restingVelocityThreshold) {
        this.restingVelocityThreshold = restingVelocityThreshold;
    }
}
