package com.davismariotti.physics.sprites;

import com.davismariotti.physics.Game;
import com.davismariotti.physics.kinematics.Vector;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class Ball extends RigidBody {

    public Ball(Vector position, Vector vector, List<Vector> forces, double coefficientOfRestitution, double dragCoefficient) {
        super(position, vector, forces, 1, false, coefficientOfRestitution, dragCoefficient);
    }

    @Override
    public void draw(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        Ellipse2D ellipse2D = new Ellipse2D.Double(this.getPosition().getX() * Game.SCALE, this.getPosition().getY() * Game.SCALE, 5, 5);

        graphics.fill(ellipse2D);
    }
}
