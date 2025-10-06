package com.davismariotti.physics.rendering;

import com.davismariotti.physics.kinematics.Vector;

/**
 * Handles conversion between world space (physics coordinates) and screen space (pixel coordinates)
 * Centralizes coordinate transformation logic for consistent rendering
 */
public class Camera {
    private final double scale;
    private final int screenHeight;

    public Camera(double scale, int screenHeight) {
        this.scale = scale;
        this.screenHeight = screenHeight;
    }

    /**
     * Convert world coordinates to screen coordinates
     * Applies scaling and Y-axis flip (world Y increases upward, screen Y increases downward)
     */
    public ScreenPoint worldToScreen(Vector worldPos) {
        int screenX = (int) (worldPos.getX() * scale);
        int screenY = screenHeight - (int) (worldPos.getY() * scale);
        return new ScreenPoint(screenX, screenY);
    }

    /**
     * Convert world coordinates to screen coordinates (double variant for sub-pixel precision)
     */
    public ScreenPointDouble worldToScreenDouble(Vector worldPos) {
        double screenX = worldPos.getX() * scale;
        double screenY = screenHeight - (worldPos.getY() * scale);
        return new ScreenPointDouble(screenX, screenY);
    }

    /**
     * Convert world distance/size to screen distance/size
     */
    public double worldToScreenDistance(double worldDistance) {
        return worldDistance * scale;
    }

    /**
     * Convert screen coordinates to world coordinates
     */
    public Vector screenToWorld(int screenX, int screenY) {
        double worldX = screenX / scale;
        double worldY = (screenHeight - screenY) / scale;
        return new Vector(worldX, worldY);
    }

    public double getScale() {
        return scale;
    }

    /**
     * Represents a point in screen space (pixel coordinates)
     */
    public record ScreenPoint(int x, int y) {
    }

    /**
     * Represents a point in screen space with sub-pixel precision
     */
    public static class ScreenPointDouble {
        public final double x;
        public final double y;

        public ScreenPointDouble(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
