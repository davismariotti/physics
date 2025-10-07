package com.davismariotti.physics.sprites;

import com.davismariotti.physics.collision.AABBCollider;
import com.davismariotti.physics.collision.Collider;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.rendering.Camera;

import java.awt.*;
import java.util.Collections;

/**
 * Ground body representing the floor of the simulation
 * Static body with AABB collision shape
 */
public class Ground extends RigidBody {
    private final double width;
    private final double height;

    public Ground(double x, double y, double width, double height) {
        // Static body: infinite mass (represented as Double.MAX_VALUE), no velocity
        super(
                new Vector(x, y),
                Vector.ZERO,
                Collections.emptyList(),
                Double.MAX_VALUE,
                true,  // isStatic
                1.0,   // coefficient of restitution (full bounce)
                0.0,   // no drag
                null
        );
        this.width = width;
        this.height = height;
    }

    @Override
    public Collider getCollider() {
        return new AABBCollider(this.getPosition(), width, height);
    }

    @Override
    public void draw(Graphics2D graphics, Camera camera) {
        AABBCollider collider = (AABBCollider) getCollider();
        Vector min = collider.getMin();
        Vector max = collider.getMax();

        // Convert to screen coordinates
        Camera.ScreenPoint minScreen = camera.worldToScreen(min);
        Camera.ScreenPoint maxScreen = camera.worldToScreen(max);

        // Calculate screen dimensions (note: Y is flipped in screen space)
        int screenX = minScreen.x();
        int screenY = maxScreen.y();  // Use max Y because screen Y is flipped
        int screenWidth = maxScreen.x() - minScreen.x();
        int screenHeight = minScreen.y() - maxScreen.y();

        // Draw ground as a filled rectangle
        // Green grass on top
        graphics.setColor(new Color(100, 200, 100));
        graphics.fillRect(screenX, screenY, screenWidth, 3);

        // Brown dirt below
        graphics.setColor(new Color(139, 69, 19));
        graphics.fillRect(screenX, screenY + 3, screenWidth, screenHeight - 3);
    }
}
