package com.davismariotti.physics.input;

/**
 * Reusable action debouncer that implements keyboard-style timing
 * Useful for preventing action spam while allowing controlled repetition
 *
 * Timing behavior:
 * - First trigger: executes immediately
 * - Initial delay: waits before allowing repeats
 * - Repeat: executes at regular intervals while trigger is active
 */
public class ActionDebouncer {
    private long firstTriggerTime = -1;
    private long lastActionTime = 0;
    private final long initialDelayMs;
    private final long repeatDelayMs;

    /**
     * Create an action debouncer with specified timing
     *
     * @param initialDelayMs delay before repeating starts (e.g., 500ms)
     * @param repeatDelayMs delay between repeats once started (e.g., 100ms)
     */
    public ActionDebouncer(long initialDelayMs, long repeatDelayMs) {
        this.initialDelayMs = initialDelayMs;
        this.repeatDelayMs = repeatDelayMs;
    }

    /**
     * Check if action should execute this frame
     *
     * @param shouldTrigger whether the triggering condition is true (e.g., key pressed)
     * @return true if action should execute
     */
    public boolean shouldExecute(boolean shouldTrigger) {
        long now = System.currentTimeMillis();

        if (!shouldTrigger) {
            reset();
            return false;
        }

        if (firstTriggerTime == -1) {
            // First trigger - execute immediately
            firstTriggerTime = now;
            lastActionTime = now;
            return true;
        }

        long timeSinceFirst = now - firstTriggerTime;
        long timeSinceLast = now - lastActionTime;

        if (timeSinceFirst < initialDelayMs) {
            // In initial delay period - don't execute
            return false;
        }

        if (timeSinceLast >= repeatDelayMs) {
            // Time for repeat execution
            lastActionTime = now;
            return true;
        }

        return false;
    }

    /**
     * Reset the debouncer state
     * Called automatically when trigger becomes false
     */
    public void reset() {
        firstTriggerTime = -1;
    }
}
