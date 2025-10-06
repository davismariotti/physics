package com.davismariotti.physics.core;

import com.davismariotti.physics.kinematics.Vector;
import lombok.Data;

@Data
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
}
