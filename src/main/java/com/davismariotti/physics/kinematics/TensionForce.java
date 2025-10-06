package com.davismariotti.physics.kinematics;

import com.davismariotti.physics.forces.Force;
import com.davismariotti.physics.sprites.RigidBody;

/**
 * Represents tension force (not currently used in simulation)
 */
public class TensionForce implements Force {
    private final Vector origin;
    private final RigidBody body;

    public TensionForce(Vector origin, RigidBody body) {
        this.origin = origin;
        this.body = body;
    }

    @Override
    public Vector calculate(RigidBody body) {
        return new Vector(0, 0);
    }

    public Vector getVectorBetweenPoints() {
        return new Vector(origin.x() - body.getPosition().x(), origin.y() - body.getPosition().y());
    }

    public double getAngle() {
        // Math.atan2()
        return 0;
    }

    public Vector getOrigin() {
        return origin;
    }

    public RigidBody getBody() {
        return body;
    }
}
