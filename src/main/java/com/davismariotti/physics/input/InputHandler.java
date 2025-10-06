package com.davismariotti.physics.input;

import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.interactions.InputContext;
import com.davismariotti.physics.interactions.WorldInteractionSystem;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles keyboard input and delegates to appropriate systems
 */
public class InputHandler {
    private final Set<Integer> pressed = new HashSet<>();
    private final PhysicsSimulator simulator;
    private final WorldInteractionSystem interactionSystem;

    public InputHandler(PhysicsSimulator simulator, WorldInteractionSystem interactionSystem) {
        this.simulator = simulator;
        this.interactionSystem = interactionSystem;
    }

    public synchronized void keyPressed(int keyCode) {
        pressed.add(keyCode);
    }

    public synchronized void keyReleased(int keyCode) {
        pressed.remove(keyCode);
    }

    /**
     * Process currently pressed keys and update game state
     */
    public void processInput() {
        // Create input context for interactions
        InputContext context = new InputContext(pressed, simulator);

        // Delegate to interaction system
        interactionSystem.handleInput(context);

        // Handle physics config controls
        if (pressed.size() >= 1) {
            for (int pressedCode : pressed) {
                if (pressedCode == KeyEvent.VK_UP) {
                    double newDrag = Math.min(1.0, simulator.getConfig().getDragCoefficient() + 0.001);
                    simulator.updateDragCoefficient(newDrag);
                }
                if (pressedCode == KeyEvent.VK_DOWN) {
                    double newDrag = Math.max(0.0, simulator.getConfig().getDragCoefficient() - 0.001);
                    simulator.updateDragCoefficient(newDrag);
                }
                if (pressedCode == KeyEvent.VK_R) {
                    double newRestitution = Math.min(1.0, simulator.getConfig().getCoefficientOfRestitution() + 0.05);
                    simulator.updateCoefficientOfRestitution(newRestitution);
                }
                if (pressedCode == KeyEvent.VK_F) {
                    double newRestitution = Math.max(0.0, simulator.getConfig().getCoefficientOfRestitution() - 0.05);
                    simulator.updateCoefficientOfRestitution(newRestitution);
                }
                if (pressedCode == KeyEvent.VK_Q) {
                    System.exit(0);
                }
            }
        }
    }
}
