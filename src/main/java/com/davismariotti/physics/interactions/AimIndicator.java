package com.davismariotti.physics.interactions;

import com.davismariotti.physics.components.Ray;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.rendering.Camera;
import com.davismariotti.physics.sprites.Ball;
import lombok.Getter;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Collections;

/**
 * Interactive aim indicator that can spawn balls
 * Composes a Ray for visual representation
 */
public class AimIndicator implements WorldInteraction {
    @Getter
    private final Ray ray;

    public AimIndicator(Vector position, int length, double thetaDegrees, int distance) {
        this.ray = Ray.withDegrees(position, length, thetaDegrees, distance);
    }

    @Override
    public void update(double epsilon) {
        // No physics update needed for aim indicator
    }

    @Override
    public void handleInput(InputContext context) {
        if (context.isKeyPressed(KeyEvent.VK_LEFT)) {
            ray.addAngleDegrees(3);
        }
        if (context.isKeyPressed(KeyEvent.VK_RIGHT)) {
            ray.addAngleDegrees(-3);
        }
        if (context.isKeyPressed(KeyEvent.VK_ENTER)) {
            spawnBall(context);
        }
    }

    @Override
    public void draw(Graphics2D graphics, Camera camera) {
        ray.draw(graphics, camera);
    }

    private void spawnBall(InputContext context) {
        Ball ball = new Ball(
                ray.getEndPosition(), // Spawn at end of launcher, not at base
                ray.getUnitVector().multiply(40),
                Collections.singletonList(context.getSimulator().getConfig().getGravity()),
                context.getSimulator().getConfig().getCoefficientOfRestitution(),
                context.getSimulator().getConfig().getDragCoefficient()
        );
        context.getSimulator().addBody(ball);
    }
}
