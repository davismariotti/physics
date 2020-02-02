package com.davismariotti.physics.kinematics;

import com.davismariotti.physics.sprites.RigidBody;
import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public class TensionForce extends Vector {

    private Vector origin;
    private RigidBody body;

    public TensionForce(Vector origin, RigidBody body) {
        super(0, 0);
        this.origin = origin;
        this.body = body;
    }

    public Vector getVectorBetweenPoints() {
        return new Vector(origin.getX() - body.getPosition().getX(), origin.getY() - body.getPosition().getY());
    }

    public double getAngle() {
//        Math.atan2()

        return 0;
    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }
}
