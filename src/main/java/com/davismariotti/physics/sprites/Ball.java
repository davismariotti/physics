package com.davismariotti.physics.sprites;

import com.davismariotti.physics.collision.CircleCollider;
import com.davismariotti.physics.collision.Collider;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.rendering.Camera;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;

public class Ball extends DynamicBody {
    private static final double DEFAULT_RADIUS = 0.25;
    private static final double DENSITY = 1.0;  // Density constant for all balls
    private final double radius;

    public Ball(Vector position, Vector vector, List<Vector> forces, MaterialProperties material, double radius) {
        super(position, vector, forces, calculateMass(radius), material);
        this.radius = radius;
    }

    private static double calculateMass(double radius) {
        return 1.0;
    }

    // Constructor without radius (uses default)
    public Ball(Vector position, Vector vector, List<Vector> forces, MaterialProperties material) {
        this(position, vector, forces, material, DEFAULT_RADIUS);
    }

    // Legacy constructor for backward compatibility
    public Ball(Vector position, Vector vector, List<Vector> forces, double coefficientOfRestitution, double dragCoefficient) {
        this(position, vector, forces, new MaterialProperties(coefficientOfRestitution, dragCoefficient, 0.3, 0.2), DEFAULT_RADIUS);
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public Collider getCollider() {
        return new CircleCollider(this.getPosition(), radius);
    }

    @Override
    public void draw(Graphics2D graphics, Camera camera) {
        // Convert world coordinates to screen coordinates
        Camera.ScreenPointDouble screenPos = camera.worldToScreenDouble(this.getPosition());
        double screenRadius = camera.worldToScreenDistance(this.radius);

        // Center the circle on the position
        double x = screenPos.x() - screenRadius;
        double y = screenPos.y() - screenRadius;
        double diameter = screenRadius * 2;

        // Draw ball with gradient for 3D effect
        Ellipse2D ellipse = new Ellipse2D.Double(x, y, diameter, diameter);

        // Create radial gradient for shading
        Point2D center = new Point2D.Double(screenPos.x() - screenRadius * 0.3, screenPos.y() - screenRadius * 0.3);
        float screenRadiusFloat = (float) screenRadius;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {new Color(255, 100, 100), new Color(200, 50, 50)};
        RadialGradientPaint gradient = new RadialGradientPaint(center, screenRadiusFloat, dist, colors);

        graphics.setPaint(gradient);
        graphics.fill(ellipse);

        // Add outline
        graphics.setColor(new Color(150, 30, 30));
        graphics.setStroke(new BasicStroke(1));
        graphics.draw(ellipse);
    }
}
