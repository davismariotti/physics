package com.davismariotti.physics.core;

import com.davismariotti.physics.kinematics.Vector;

public class PhysicsConfig {
    private Vector gravity;
    private double scale;
    private double gameSpeed;
    private double coefficientOfRestitution;
    private double dragCoefficient;

    public PhysicsConfig() {
        this.gravity = new Vector(0, -9.8);
        this.scale = 10.0;
        this.gameSpeed = 3.0;
        this.coefficientOfRestitution = 0.9;
        this.dragCoefficient = 0.0;
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

    public double getCoefficientOfRestitution() {
        return coefficientOfRestitution;
    }

    public void setCoefficientOfRestitution(double coefficientOfRestitution) {
        this.coefficientOfRestitution = coefficientOfRestitution;
    }

    public double getDragCoefficient() {
        return dragCoefficient;
    }

    public void setDragCoefficient(double dragCoefficient) {
        this.dragCoefficient = dragCoefficient;
    }
}
