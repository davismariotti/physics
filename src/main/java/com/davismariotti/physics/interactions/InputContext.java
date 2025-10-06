package com.davismariotti.physics.interactions;

import com.davismariotti.physics.core.PhysicsSimulator;

import java.util.Set;

/**
 * Context object that provides interactions with access to input state and the physics world
 */
public record InputContext(Set<Integer> pressedKeys, PhysicsSimulator simulator) {

    /**
     * Check if a key is currently pressed
     */
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }
}
