package com.davismariotti.physics.kinematics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vector {

    public static Vector ZERO = new Vector();

    double xComponent = 0;
    double yComponent = 0;

    public Vector add(Vector other) {
        return new Vector(xComponent + other.getXComponent(), yComponent + other.getYComponent());
    }

    public Vector multiply(double x, double y) {
        return new Vector(this.xComponent * x, this.yComponent * y);
    }

    public Position applyToLocation(Position old) {
        Position position = new Position();
        position.setX(old.getX() + (int) xComponent);
        position.setY(old.getY() + (int) yComponent);

        return position;
    }

    public Vector getUnitVector() {
        Vector unitVector = new Vector();
        unitVector.setXComponent(xComponent / Math.sqrt(Math.pow(xComponent, 2) + Math.pow(yComponent, 2)));
        unitVector.setYComponent(yComponent / Math.sqrt(Math.pow(xComponent, 2) + Math.pow(yComponent, 2)));
        return unitVector;
    }
}
