package com.davismariotti.physics.kinematics;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Vector {

    public static Vector ZERO = new Vector(0, 0);

    double x;
    double y;

    public Vector add(Vector other) {
        return new Vector(x + other.getX(), y + other.getY());
    }

    public Vector multiply(double x, double y) {
        return new Vector(this.x * x, this.y * y);
    }

    public Vector getUnitVector() {
        double denominator = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        return new Vector(x / denominator, y / denominator);
    }

    public double getMagnitude() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }
}
