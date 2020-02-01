package com.davismariotti.physics.kinematics;

import lombok.Value;

@Value
public class Vector {

    public static Vector ZERO = new Vector(0, 0);

    double x;
    double y;

    public Vector add(Vector other) {
        return new Vector(x + other.getX(), y + other.getY());
    }

    public Vector multiply(double s) {
        return new Vector(this.x * s, this.y * s);
    }

    public Vector getUnitVector() {
        double denominator = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        return new Vector(x / denominator, y / denominator);
    }

    public double getMagnitude() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }
}
