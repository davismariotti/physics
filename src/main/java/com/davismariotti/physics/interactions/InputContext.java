package com.davismariotti.physics.interactions;

import com.davismariotti.physics.core.PhysicsSimulator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * Context object that provides interactions with access to input state and the physics world
 */
@Data
@AllArgsConstructor
public class InputContext {
    private final Set<Integer> pressedKeys;
    private final PhysicsSimulator simulator;

    /**
     * Check if a key is currently pressed
     */
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }
}
