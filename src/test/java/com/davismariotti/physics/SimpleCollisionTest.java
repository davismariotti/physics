package com.davismariotti.physics;

import com.davismariotti.physics.core.PhysicsConfig;
import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.Ball;
import com.davismariotti.physics.sprites.DynamicBody;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * Simple test to debug ball collision behavior frame-by-frame
 */
class SimpleCollisionTest {

    @Test
    void testSingleCollisionDebug() {
        PhysicsConfig config = new PhysicsConfig();
        config.setGravity(new Vector(0, 0));  // No gravity for simplicity
        config.setSubsteps(1);  // Single substep for clarity

        PhysicsSimulator simulator = new PhysicsSimulator(config);

        // Ball A: moving right slowly
        Ball ballA = new Ball(
                new Vector(30, 50),
                new Vector(5, 0),
                Collections.emptyList(),
                1.0,
                0.0
        );

        // Ball B: stationary, close enough to collide soon
        Ball ballB = new Ball(
                new Vector(50, 50),  // 20 units away, radius 5 each = collision at distance 10
                Vector.ZERO,
                Collections.emptyList(),
                1.0,
                0.0
        );

        simulator.addBody(ballA);
        simulator.addBody(ballB);

        DynamicBody ballADynamic = (DynamicBody) ballA;
        DynamicBody ballBDynamic = (DynamicBody) ballB;

        System.out.println("\n=== Frame-by-Frame Collision Debug ===\n");

        double timestep = 1.0 / 60.0;

        for (int frame = 0; frame < 150; frame++) {
            double posAX_before = ballADynamic.getPosition().x();
            double velAX_before = ballADynamic.getVelocity().x();
            double velBX_before = ballBDynamic.getVelocity().x();

            simulator.update(timestep);

            double posAX_after = ballADynamic.getPosition().x();
            double posBX_after = ballBDynamic.getPosition().x();
            double velAX_after = ballADynamic.getVelocity().x();
            double velBX_after = ballBDynamic.getVelocity().x();

            double distance = posBX_after - posAX_after;

            // Only print when something interesting happens
            if (distance < 15 || Math.abs(velAX_after - velAX_before) > 0.1 || Math.abs(velBX_after - velBX_before) > 0.1) {
                System.out.printf("Frame %3d: dist=%.3f  A_vel=(%.3f -> %.3f)  B_vel=(%.3f -> %.3f)%n",
                        frame, distance, velAX_before, velAX_after, velBX_before, velBX_after);
            }

            // Stop after collision clearly happened
            if (velBX_after > 1.0) {
                System.out.println("\nCollision detected - Ball B now moving");
                System.out.printf("Final: A_vel=%.3f, B_vel=%.3f%n", velAX_after, velBX_after);
                break;
            }

            // Stop if velocities explode
            if (Math.abs(velAX_after) > 50 || Math.abs(velBX_after) > 50) {
                System.out.println("\n⚠️  VELOCITY EXPLOSION DETECTED");
                System.out.printf("Frame %d: A_vel=%.3f, B_vel=%.3f%n", frame, velAX_after, velBX_after);
                break;
            }
        }
    }
}
