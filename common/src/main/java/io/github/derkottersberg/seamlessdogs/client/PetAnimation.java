package io.github.derkottersberg.seamlessdogs.client;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;

/** Original procedural clip retargeted to vanilla arms/head/tail, in ticks. */
public final class PetAnimation {
    private PetAnimation() { }
    public record Sample(float weight, float stroke) { }
    public static Sample sample(float ticks) {
        if (!Float.isFinite(ticks) || ticks < 0 || ticks >= SeamlessDogs.DURATION) return new Sample(0, 0);
        float in = smooth(Math.min(1, ticks / 6));
        float out = smooth(Math.min(1, (SeamlessDogs.DURATION - ticks) / 8));
        return new Sample(in * out, (float) Math.sin(ticks * Math.PI / 8));
    }
    private static float smooth(float value) { return value * value * (3 - 2 * value); }
}
