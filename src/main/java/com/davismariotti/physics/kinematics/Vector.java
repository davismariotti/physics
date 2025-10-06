package com.davismariotti.physics.kinematics;

public record Vector(double x, double y) {

    public static Vector ZERO = new Vector(0, 0);

    public Vector add(Vector other) {
        return new Vector(x + other.x, y + other.y);
    }

    public Vector multiply(double s) {
        return new Vector(this.x * s, this.y * s);
    }

    public Vector getUnitVector() {
        double denominator = Math.sqrt(x * x + y * y);
        if (denominator == 0) {
            return ZERO;
        }
        return new Vector(x / denominator, y / denominator);
    }

    public double getMagnitude() {
        return Math.sqrt(x * x + y * y);
    }
}
