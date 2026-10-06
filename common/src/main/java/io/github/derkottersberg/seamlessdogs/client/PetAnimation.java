package io.github.derkottersberg.seamlessdogs.client;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;

/** Original procedural clip retargeted to vanilla arms/head/tail, in ticks. */
public final class PetAnimation {
    private PetAnimation() { }
    public record Sample(float weight, float stroke) { }
    public static Sample sample(float ticks) {
        if (!Float.isFinite(ticks) || ticks < 0 || ticks >= SeamlessDogs.DURATION) return new Sample(0, 0);
        float in = smooth(Math.min(1, ticks / 6));
        float out = settle(Math.min(1, (SeamlessDogs.DURATION - ticks) / 8));
        // Finish the last stroke before withdrawing; a moving stroke under a
        // shrinking reach makes the hand wobble on its way back to rest.
        float stroke = ticks >= 32 ? 0 : (float) Math.sin(ticks * Math.PI / 8)
            * settle(Math.max(0, Math.min(1, (32 - ticks) / 4)));
        return new Sample(in * out, stroke);
    }
    public static float vanillaSwing(float swing, float weight) {
        float rest = 1 - Math.max(0, Math.min(1, weight));
        // Vanilla arm translation uses sqrt(swing). Squaring the blend keeps
        // that translation smooth as vanilla motion reappears.
        return swing * rest * rest;
    }
    static float settle(float value) {
        double t=value;
        return (float)Math.max(0,Math.min(1,t*t*t*(t*(t*6-15)+10)));
    }
    private static float smooth(float value) { return value * value * (3 - 2 * value); }
}
