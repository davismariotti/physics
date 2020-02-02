package com.davismariotti.physics.kinematics;

import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
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
        double denominator = Math.sqrt(x * x + y * y);
        return new Vector(x / denominator, y / denominator);
    }

    public double getMagnitude() {
        return Math.sqrt(x * x + y * y);
    }
}
