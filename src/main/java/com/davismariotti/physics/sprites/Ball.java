package com.davismariotti.physics.sprites;

import com.davismariotti.physics.collision.CircleCollider;
import com.davismariotti.physics.collision.Collider;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.rendering.Camera;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;

public class Ball extends RigidBody {
    private static final double RADIUS = 0.5; // radius in world units

    public Ball(Vector position, Vector vector, List<Vector> forces, double coefficientOfRestitution, double dragCoefficient) {
        super(position, vector, forces, 1, false, coefficientOfRestitution, dragCoefficient, null);
    }

    @Override
    public Collider getCollider() {
        return new CircleCollider(this.getPosition(), RADIUS);
    }

    @Override
    public void draw(Graphics2D graphics, Camera camera) {
        // Convert world coordinates to screen coordinates
        Camera.ScreenPointDouble screenPos = camera.worldToScreenDouble(this.getPosition());
        double screenRadius = camera.worldToScreenDistance(RADIUS);

        // Center the circle on the position
        double x = screenPos.x() - screenRadius;
        double y = screenPos.y() - screenRadius;
        double diameter = screenRadius * 2;

        // Draw ball with gradient for 3D effect
        Ellipse2D ellipse = new Ellipse2D.Double(x, y, diameter, diameter);

        // Create radial gradient for shading
        Point2D center = new Point2D.Double(screenPos.x() - screenRadius * 0.3, screenPos.y() - screenRadius * 0.3);
        float radius = (float) screenRadius;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {new Color(255, 100, 100), new Color(200, 50, 50)};
        RadialGradientPaint gradient = new RadialGradientPaint(center, radius, dist, colors);

        graphics.setPaint(gradient);
        graphics.fill(ellipse);

        // Add outline
        graphics.setColor(new Color(150, 30, 30));
        graphics.setStroke(new BasicStroke(1));
        graphics.draw(ellipse);
    }
}
