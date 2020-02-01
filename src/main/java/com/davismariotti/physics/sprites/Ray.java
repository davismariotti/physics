package com.davismariotti.physics.sprites;

import com.davismariotti.physics.kinematics.Vector;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.Collections;

@Getter
@Setter
public class Ray extends RigidBody {
    // The position is the point the ray is centered at

    private int length;
    private double theta;
    private int distance;

    public Ray(Vector position, int length, double theta, int distance) {
        super(position, Vector.ZERO, Collections.emptyList(), 1);
        this.length = length;
        this.theta = degreesToRadians(theta);
        this.distance = distance;
    }

    @Override
    public void draw(Graphics2D graphics) {
        Vector start = getStartPosition();
        Vector end = getEndPosition();
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(3));
        graphics.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
    }

    @Override
    public void update(double epsilon) {
    }

    private Vector getStartPosition() {
        double x = distance * Math.cos(theta);
        double y = distance * Math.sin(theta);
        return new Vector((int) Math.round(x), (int) Math.round(y)).add(this.getPosition());
    }

    private Vector getEndPosition() {
        double x = (distance + length) * Math.cos(theta);
        double y = (distance + length) * Math.sin(theta);
        return new Vector((int) Math.round(x), (int) Math.round(y)).add(this.getPosition());
    }

    public void addAngle(int angle) {
        this.theta += degreesToRadians(angle);
    }

    public static double degreesToRadians(double degrees) {
        return degrees * (Math.PI / 180);
    }

    public Vector getUnitVector() {
        Vector fullVector = new Vector(getEndPosition().getX() - getStartPosition().getX(), getEndPosition().getY() - getStartPosition().getY());
        return fullVector.getUnitVector();
    }
}
