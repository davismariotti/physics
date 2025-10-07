package com.davismariotti.physics.sprites;

/**
 * Material properties for physics objects
 * Encapsulates all physical coefficients that define how an object behaves in collisions
 */
public record MaterialProperties(
        double coefficientOfRestitution,  // Bounciness (0 = no bounce, 1 = perfect bounce)
        double dragCoefficient,            // Air/fluid resistance
        double staticFriction,             // Resistance to starting motion
        double dynamicFriction             // Resistance while sliding
) {
    /**
     * Default material properties (moderate bounce, no drag, low friction)
     */
    public static final MaterialProperties DEFAULT = new MaterialProperties(0.9, 0.0, 0.3, 0.2);

    /**
     * Rubber-like material (high bounce, low friction)
     */
    public static final MaterialProperties RUBBER = new MaterialProperties(0.95, 0.0, 0.4, 0.3);

    /**
     * Wood-like material (medium bounce, medium friction)
     */
    public static final MaterialProperties WOOD = new MaterialProperties(0.6, 0.0, 0.5, 0.35);

    /**
     * Metal-like material (low bounce, low friction)
     */
    public static final MaterialProperties METAL = new MaterialProperties(0.3, 0.0, 0.2, 0.15);

    /**
     * Ice-like material (medium bounce, very low friction)
     */
    public static final MaterialProperties ICE = new MaterialProperties(0.5, 0.0, 0.1, 0.05);

    /**
     * Combine two materials using Pythagorean theorem (common in physics engines)
     */
    public static MaterialProperties combine(MaterialProperties a, MaterialProperties b) {
        return new MaterialProperties(
                Math.min(a.coefficientOfRestitution, b.coefficientOfRestitution),
                (a.dragCoefficient + b.dragCoefficient) / 2.0,
                Math.sqrt(a.staticFriction * a.staticFriction + b.staticFriction * b.staticFriction),
                Math.sqrt(a.dynamicFriction * a.dynamicFriction + b.dynamicFriction * b.dynamicFriction)
        );
    }
}
