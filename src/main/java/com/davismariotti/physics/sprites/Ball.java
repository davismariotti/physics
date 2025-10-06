package com.davismariotti.physics.sprites;

import com.davismariotti.physics.Game;
import com.davismariotti.physics.kinematics.Vector;

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
    public void draw(Graphics2D graphics) {
        // Convert world coordinates to screen coordinates
        double screenX = this.getPosition().getX() * Game.SCALE;
        double screenY = this.getPosition().getY() * Game.SCALE;
        double screenRadius = RADIUS * Game.SCALE;

        // Center the circle on the position
        double x = screenX - screenRadius;
        double y = screenY - screenRadius;
        double diameter = screenRadius * 2;

        // Draw ball with gradient for 3D effect
        Ellipse2D ellipse = new Ellipse2D.Double(x, y, diameter, diameter);

        // Create radial gradient for shading
        Point2D center = new Point2D.Double(screenX - screenRadius * 0.3, screenY - screenRadius * 0.3);
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
