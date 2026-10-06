package io.github.derkottersberg.seamlessdogs.client;

/** Render time for server-selected clips, including their interrupted recovery. */
public final class AnimationTimeline {
    private AnimationTimeline() { }
    public static final class Playback {
        private final long start;
        private long stop=-1;
        private float elapsed,recovery;
        public Playback(long start) { this.start=start; }
        public float elapsed(long now,float partial) {
            if(stop<0)elapsed=Math.max(elapsed,AnimationTimeline.elapsed(start,-1,now,partial));
            return elapsed;
        }
        public void stop(long now) {
            if(stop>=0)return;
            // Retain the latest rendered fraction. Freezing at the integer
            // tick would rewind a nearly completed withdrawal by up to a tick.
            elapsed(now,0);stop=now;
        }
        public float fade(long now,float partial) {
            if(stop<0)return 1;
            recovery=Math.max(recovery,now-stop+fraction(partial));
            return 1-PetAnimation.settle(Math.max(0,Math.min(1,recovery/6F)));
        }
        public boolean expired(long now,int duration) {
            return stop>=0?now-stop>=6:now-start>=duration;
        }
    }
    public static float elapsed(long start, long stop, long now, float partial) {
        // A stopped pose must stay frozen. Adding partial to the stopped tick
        // would replay 0..1 ticks of motion every tick throughout recovery.
        return stop >= 0 ? stop - start : now - start + fraction(partial);
    }
    public static float fade(long stop, long now, float partial) {
        if (stop < 0) return 1;
        float time = Math.max(0, Math.min(1, (now - stop + fraction(partial)) / 6F));
        return 1 - PetAnimation.settle(time);
    }
    private static float fraction(float partial) {
        return Float.isFinite(partial) ? Math.max(0, Math.min(1, partial)) : 0;
    }
}
