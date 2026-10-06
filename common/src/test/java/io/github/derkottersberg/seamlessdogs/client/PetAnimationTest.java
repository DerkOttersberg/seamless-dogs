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
    @Test void withdrawalHasNoStrokeAndSettlesWithoutOvershoot() {
        float previous=1;
        for(int frame=3200;frame<=4000;frame++) {
            var sample=PetAnimation.sample(frame/100F);
            assertEquals(0,sample.stroke());
            assertTrue(sample.weight()<=previous+0.000002F);
            previous=sample.weight();
        }
        assertTrue(PetAnimation.sample(39.9F).weight()<.00003F,"Return must reach rest without a last-frame snap");
        assertTrue(1-PetAnimation.sample(32.1F).weight()<.00003F,"Return must start without a jerk");
    }
    @Test void vanillaSwingComesBackContinuouslyForBothArms() {
        for(float swing:new float[]{0,.2F,.5F,.9F,1}) {
            assertEquals(swing,PetAnimation.vanillaSwing(swing,0));
            assertEquals(0,PetAnimation.vanillaSwing(swing,1));
            float previous=0;
            for(int frame=0;frame<=100;frame++) {
                float blended=PetAnimation.vanillaSwing(swing,1-frame/100F);
                assertTrue(blended>=previous);
                assertTrue(Math.sqrt(blended)-Math.sqrt(previous)<=.01001);
                previous=blended;
            }
        }
    }
}
