package com.davismariotti.physics.components;

import com.davismariotti.physics.Game;
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
    private int length; // in world units
    private double theta; // angle in radians
    private int distance; // in world units, offset from position

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
     * Draw the ray as a launcher
     * Converts world coordinates to screen coordinates for proper display
     */
    public void draw(Graphics2D graphics) {
        Vector start = getStartPosition();
        Vector end = getEndPosition();

        // Convert world coordinates to screen coordinates
        int screenPosX = (int) (position.getX() * Game.SCALE);
        int screenPosY = (int) (position.getY() * Game.SCALE);
        int screenStartX = (int) (start.getX() * Game.SCALE);
        int screenStartY = (int) (start.getY() * Game.SCALE);
        int screenEndX = (int) (end.getX() * Game.SCALE);
        int screenEndY = (int) (end.getY() * Game.SCALE);

        // Draw launcher base (pivot point)
        graphics.setColor(new Color(100, 100, 100)); // Dark gray
        graphics.fillOval(screenPosX - 3, screenPosY - 3, 6, 6);

        // Draw launcher barrel with outline
        graphics.setColor(new Color(180, 180, 180)); // Light gray barrel
        graphics.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.drawLine(screenStartX, screenStartY, screenEndX, screenEndY);

        // Draw barrel outline
        graphics.setColor(new Color(80, 80, 80)); // Darker outline
        graphics.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.drawLine(screenStartX, screenStartY, screenEndX, screenEndY);

        // Redraw barrel on top
        graphics.setColor(new Color(180, 180, 180));
        graphics.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.drawLine(screenStartX, screenStartY, screenEndX, screenEndY);

        // Draw directional indicator at the end
        graphics.setColor(new Color(255, 150, 0)); // Orange tip
        graphics.fillOval(screenEndX - 4, screenEndY - 4, 8, 8);
    }

    private static double degreesToRadians(double degrees) {
        return degrees * (Math.PI / 180);
    }
}
