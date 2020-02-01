package com.davismariotti.physics.sprites;

import com.davismariotti.physics.Game;
import com.davismariotti.physics.kinematics.Vector;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class Ball extends RigidBody {

    public Ball(Vector position, Vector vector, List<Vector> forces) {
        super(position, vector, forces, 1);
    }

    @Override
    public void draw(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        Ellipse2D ellipse2D = new Ellipse2D.Double(this.getPosition().getX() * Game.SCALE, this.getPosition().getY() * Game.SCALE, 10, 10);

        graphics.fill(ellipse2D);
    }

    @Override
    public void update(double epsilon) {
        Vector force = this.getResultantForce();
        Vector acceleration = new Vector(force.getX() / this.getMass(), force.getY() / this.getMass());
        setVelocity(getVelocity().add(new Vector(acceleration.getX() * epsilon, acceleration.getY() * epsilon)));
        setPosition(getPosition().add(new Vector(getVelocity().getX() * epsilon, getVelocity().getY() * epsilon)));
    }
}
