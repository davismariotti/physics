package com.davismariotti.physics.components;

import com.davismariotti.physics.kinematics.Vector;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.*;

/**
 * Pure geometry component representing a ray/line with direction
 * Can be composed into interactive elements like aim indicators
 */
@Data
@AllArgsConstructor
public class Ray {
    private Vector position;
    private int length;
    private double theta; // angle in radians
    private int distance; // offset from position

    /**
     * Create a ray with angle in degrees
     */
    public static Ray withDegrees(Vector position, int length, double degrees, int distance) {
        return new Ray(position, length, degreesToRadians(degrees), distance);
    }

    /**
     * Get the start point of the ray
     */
    public Vector getStartPosition() {
        double x = distance * Math.cos(theta);
        double y = distance * Math.sin(theta);
        return new Vector(x, y).add(position);
    }

    /**
     * Get the end point of the ray
     */
    public Vector getEndPosition() {
        double x = (distance + length) * Math.cos(theta);
        double y = (distance + length) * Math.sin(theta);
        return new Vector(x, y).add(position);
    }

    /**
     * Get the unit vector in the direction of the ray
     */
    public Vector getUnitVector() {
        Vector fullVector = new Vector(
                getEndPosition().getX() - getStartPosition().getX(),
                getEndPosition().getY() - getStartPosition().getY()
        );
        return fullVector.getUnitVector();
    }

    /**
     * Add an angle to the ray (in degrees)
     */
    public void addAngleDegrees(double degrees) {
        this.theta += degreesToRadians(degrees);
    }

    /**
     * Draw the ray
     */
    public void draw(Graphics2D graphics) {
        Vector start = getStartPosition();
        Vector end = getEndPosition();
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(3));
        graphics.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
    }

    private static double degreesToRadians(double degrees) {
        return degrees * (Math.PI / 180);
    }
}
