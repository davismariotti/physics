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
    private boolean useSpatialPartitioning;   // Enable spatial grid for broad-phase collision
    private double gridCellSize;              // Size of spatial grid cells (in world units)
    private boolean useSleeping;              // Enable sleeping for settled bodies
    private double sleepVelocityThreshold;    // Velocity below which bodies can sleep
    private int sleepFramesRequired;          // Consecutive low-velocity frames required to sleep
    private int velocityIterations;           // Number of velocity solver iterations for stability

    public PhysicsConfig() {
        this.gravity = new Vector(0, -9.8);
        this.scale = 10.0;
        this.gameSpeed = 3.0;
        this.defaultMaterial = MaterialProperties.DEFAULT;
        this.substeps = 6;
        this.restingVelocityThreshold = 0.5;
        this.useSpatialPartitioning = true;   // Enable by default for performance
        this.gridCellSize = 0.5;             // 2x max ball diameter (max radius = 10)
        this.useSleeping = true;              // Enable by default for performance
        this.sleepVelocityThreshold = 0.5;    // Sleep when speed < 0.5 (relaxed for jitter tolerance)
        this.sleepFramesRequired = 30;        // Require 30 frames (~0.5 second) of rest
        this.velocityIterations = 1;          // Disabled for now (set to 1)
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

    public boolean isUseSpatialPartitioning() {
        return useSpatialPartitioning;
    }

    public void setUseSpatialPartitioning(boolean useSpatialPartitioning) {
        this.useSpatialPartitioning = useSpatialPartitioning;
    }

    public double getGridCellSize() {
        return gridCellSize;
    }

    public void setGridCellSize(double gridCellSize) {
        this.gridCellSize = gridCellSize;
    }

    public boolean isUseSleeping() {
        return useSleeping;
    }

    public void setUseSleeping(boolean useSleeping) {
        this.useSleeping = useSleeping;
    }

    public double getSleepVelocityThreshold() {
        return sleepVelocityThreshold;
    }

    public void setSleepVelocityThreshold(double sleepVelocityThreshold) {
        this.sleepVelocityThreshold = sleepVelocityThreshold;
    }

    public int getSleepFramesRequired() {
        return sleepFramesRequired;
    }

    public void setSleepFramesRequired(int sleepFramesRequired) {
        this.sleepFramesRequired = sleepFramesRequired;
    }

    public int getVelocityIterations() {
        return velocityIterations;
    }

    public void setVelocityIterations(int velocityIterations) {
        this.velocityIterations = velocityIterations;
    }
}
