package com.sorrowmist.uselesscore.client.ui;

/** Frame-rate independent transitions, with state owned by each control. */
public final class UiMotion {
    private float value;
    private long last = System.nanoTime();
    public UiMotion(float initial) { value = initial; }
    public float to(float target) {
        long now = System.nanoTime();
        double dt = Math.min(.05, Math.max(0, (now - last) / 1_000_000_000d));
        last = now;
        value += (target - value) * (float) (1 - Math.exp(-dt * 19));
        if (Math.abs(target - value) < .001f) value = target;
        return value;
    }
    public void snap(float value) { this.value = value; last = System.nanoTime(); }
}
