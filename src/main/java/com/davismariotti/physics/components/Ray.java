package com.davismariotti.physics.components;

import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.rendering.Camera;

import java.awt.*;

/**
 * Pure geometry component representing a ray/line with direction
 * Can be composed into interactive elements like aim indicators
 */
public class Ray {
    private Vector position;
    private int length; // in world units
    private double theta; // angle in radians
    private int distance; // in world units, offset from position

    public Ray(Vector position, int length, double theta, int distance) {
        this.position = position;
        this.length = length;
        this.theta = theta;
        this.distance = distance;
    }

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
                getEndPosition().x() - getStartPosition().x(),
                getEndPosition().y() - getStartPosition().y()
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
     * Draw the ray as a launcher
     * Converts world coordinates to screen coordinates for proper display
     */
    public void draw(Graphics2D graphics, Camera camera) {
        Vector start = getStartPosition();
        Vector end = getEndPosition();

        // Convert world coordinates to screen coordinates
        Camera.ScreenPoint screenPos = camera.worldToScreen(position);
        Camera.ScreenPoint screenStart = camera.worldToScreen(start);
        Camera.ScreenPoint screenEnd = camera.worldToScreen(end);

        // Draw launcher base (pivot point)
        graphics.setColor(new Color(100, 100, 100)); // Dark gray
        graphics.fillOval(screenPos.x() - 3, screenPos.y() - 3, 6, 6);

        // Draw launcher barrel with outline
        graphics.setColor(new Color(180, 180, 180)); // Light gray barrel
        graphics.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.drawLine(screenStart.x(), screenStart.y(), screenEnd.x(), screenEnd.y());

        // Draw barrel outline
        graphics.setColor(new Color(80, 80, 80)); // Darker outline
        graphics.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.drawLine(screenStart.x(), screenStart.y(), screenEnd.x(), screenEnd.y());

        // Redraw barrel on top
        graphics.setColor(new Color(180, 180, 180));
        graphics.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.drawLine(screenStart.x(), screenStart.y(), screenEnd.x(), screenEnd.y());

        // Draw directional indicator at the end
        graphics.setColor(new Color(255, 150, 0)); // Orange tip
        graphics.fillOval(screenEnd.x() - 4, screenEnd.y() - 4, 8, 8);
    }

    private static double degreesToRadians(double degrees) {
        return degrees * (Math.PI / 180);
    }

    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
