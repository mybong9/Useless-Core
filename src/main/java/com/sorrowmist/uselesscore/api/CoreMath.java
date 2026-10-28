package com.sorrowmist.uselesscore.api;

public final class CoreMath {
    private CoreMath() {}
    public static int clamp(int value, int minimum, int maximum) {
        if (minimum > maximum) throw new IllegalArgumentException("Invalid clamp range");
        return Math.max(minimum, Math.min(maximum, value));
    }
    public static float clamp(float value, float minimum, float maximum) {
        if (minimum > maximum) throw new IllegalArgumentException("Invalid clamp range");
        return Math.max(minimum, Math.min(maximum, value));
    }
    public static double clamp(double value, double minimum, double maximum) {
        if (minimum > maximum) throw new IllegalArgumentException("Invalid clamp range");
        return Math.max(minimum, Math.min(maximum, value));
    }
    public static long clamp(long value, long minimum, long maximum) {
        if (minimum > maximum) throw new IllegalArgumentException("Invalid clamp range");
        return Math.max(minimum, Math.min(maximum, value));
    }
}
