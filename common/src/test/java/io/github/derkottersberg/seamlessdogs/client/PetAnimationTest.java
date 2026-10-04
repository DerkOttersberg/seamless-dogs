package io.github.derkottersberg.seamlessdogs.client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PetAnimationTest {
    @Test void endpointsAndMalformedTimeDoNotLeaveArmRaised() {
        for (float time : new float[] {-1, 0, 40, 500, Float.NaN, Float.POSITIVE_INFINITY}) assertEquals(0, PetAnimation.sample(time).weight());
    }
    @Test void clipIsBoundedAndReturnsSmoothlyToVanilla() {
        float previous = 0;
        for (int frame = 0; frame <= 400; frame++) {
            var sample = PetAnimation.sample(frame / 10F);
            assertTrue(sample.weight() >= 0 && sample.weight() <= 1);
            assertTrue(Math.abs(sample.stroke()) <= 1);
            assertTrue(Math.abs(sample.weight() - previous) < 0.03F);
            previous = sample.weight();
        }
    }
}
