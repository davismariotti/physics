package com.davismariotti.physics.sprites;

import com.davismariotti.physics.kinematics.Position;
import com.davismariotti.physics.kinematics.DeltaV;
import com.davismariotti.physics.kinematics.Direction;
import com.davismariotti.physics.kinematics.Vector;

import java.awt.*;

public class Ball extends Sprite {

    public Ball(Position position, Vector vector, DeltaV deltaV) {
        super(position, vector, deltaV);
    }

    @Override
    public void draw(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.fillOval(this.getPosition().getX(), this.getPosition().getY(), 3, 3);
    }

    @Override
    public void update() {
        // Update the vector with the current deltaV
        this.setVector(this.getDeltaV().applyToVector(this.getVector()));
        // Update the location with the current vector
        this.setPosition(this.getVector().applyToLocation(this.getPosition()));
    }

    public void move(Direction direction) {
        switch (direction) {
            case RIGHT:
                this.setVector(this.getVector().add(new Vector(1, 0)));
                break;
            case LEFT:
                this.setVector(this.getVector().add(new Vector(-1, 0)));
                break;
            default:
                break;
        }
    }
}
