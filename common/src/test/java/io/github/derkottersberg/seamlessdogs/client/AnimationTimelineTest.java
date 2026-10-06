package io.github.derkottersberg.seamlessdogs.client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnimationTimelineTest {
    @Test void stopAtTheSameTickPreservesTheLastRenderedFraction() {
        for(float phase:new float[]{19.9F,28.7F,34.2F,39.14F,39.95F}) {
            var playback=new AnimationTimeline.Playback(100);
            long now=100+(int)phase;
            float before=playback.elapsed(now,phase-(int)phase);
            playback.stop(now);
            assertEquals(before,playback.elapsed(now,0));
            float previous=PetAnimation.sample(before).weight();
            for(int frame=0;frame<=600;frame++) {
                float current=PetAnimation.sample(playback.elapsed(now+frame/100,frame%100/100F)).weight()
                    *playback.fade(now+frame/100,frame%100/100F);
                assertTrue(current<=previous+.000001F,"Stop packet must not rewind the withdrawal");previous=current;
            }
        }
    }
    @Test void renderAndTickQueriesCannotRewindPlaybackOrRecovery() {
        var playback=new AnimationTimeline.Playback(100);
        assertEquals(19.9F,playback.elapsed(119,.9F));
        assertEquals(19.9F,playback.elapsed(119,0));
        assertEquals(19.9F,playback.elapsed(118,.5F));
        playback.stop(119);
        float fade=playback.fade(120,.9F);
        assertEquals(fade,playback.fade(120,0));
        assertEquals(fade,playback.fade(119,.5F));
        playback.stop(120);
        assertEquals(0,playback.fade(125,0));
        assertTrue(playback.expired(125,40));
    }
    @Test void interruptedPoseDoesNotReplayPartialTicksDuringRecovery() {
        for(int stop:new int[]{8,19,28,34,39}) {
            var frozen=PetAnimation.sample(stop);
            float previous=Float.MAX_VALUE;
            for(int frame=0;frame<=600;frame++) {
                long now=100+stop+frame/100;
                float partial=(frame%100)/100F;
                float elapsed=AnimationTimeline.elapsed(100,100+stop,now,partial);
                assertEquals(stop,elapsed);
                var sample=PetAnimation.sample(elapsed);
                assertEquals(frozen.stroke(),sample.stroke());
                float weight=sample.weight()*AnimationTimeline.fade(100+stop,now,partial);
                assertTrue(weight<=previous+.000002F,"Tick boundaries must not kick the hand back upwards");
                previous=weight;
            }
            assertEquals(0,previous);
        }
    }
    @Test void runningTimelineCrossesTickBoundariesContinuouslyAndRejectsBadFractions() {
        assertEquals(19.99F,AnimationTimeline.elapsed(100,-1,119,.99F),.00001F);
        assertEquals(20,AnimationTimeline.elapsed(100,-1,120,0));
        assertEquals(20,AnimationTimeline.elapsed(100,-1,120,Float.NaN));
        assertEquals(21,AnimationTimeline.elapsed(100,-1,120,4));
        assertEquals(1,AnimationTimeline.fade(-1,120,.5F));
        assertEquals(0,AnimationTimeline.fade(100,120,.5F));
    }
}
