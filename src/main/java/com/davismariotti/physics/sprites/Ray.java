package com.davismariotti.physics.sprites;

import com.davismariotti.physics.kinematics.Position;
import com.davismariotti.physics.kinematics.DeltaV;
import com.davismariotti.physics.kinematics.Vector;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class Ray extends Sprite {
    // The position is the point the ray is centered at

    private int length;
    private double theta;
    private int distance;

    public Ray(Position position, int length, double theta, int distance) {
        super(position, Vector.ZERO, DeltaV.ZERO);
        this.length = length;
        this.theta = degreesToRadians(theta);
        this.distance = distance;
    }

    @Override
    public void draw(Graphics2D graphics) {
        Position start = getStartPosition();
        Position end = getEndPosition();
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(3));
        graphics.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
    }

    @Override
    public void update() {
    }

    private Position getStartPosition() {
        double x = distance * Math.cos(theta);
        double y = -distance * Math.sin(theta);
        return new Position((int) Math.round(x), (int) Math.round(y)).add(this.getPosition());
    }

    private Position getEndPosition() {
        double x = (distance + length) * Math.cos(theta);
        double y = -((distance + length) * Math.sin(theta));
        return new Position((int) Math.round(x), (int) Math.round(y)).add(this.getPosition());
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
