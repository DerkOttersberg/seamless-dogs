package io.github.derkottersberg.seamlessdogs.client;

import com.google.gson.Gson;
import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.gameplay.PetAction;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import org.slf4j.LoggerFactory;

/** Original additive keyframes. Rotations are degrees and offsets are vanilla model pixels. */
public final class AnimationClips {
    private static final Map<String, Clip> CACHE = new HashMap<>();
    private static final Gson JSON = new Gson();
    private static final float[] ZERO = new float[6];
    private AnimationClips() { }
    public record Pose(Map<String,float[]> parts) {
        public static final Pose NONE = new Pose(Map.of());
        public Pose scaled(float weight,float offsetScale) {
            Map<String,float[]> result=new HashMap<>();
            parts.forEach((name,p)->{float[] copy=new float[6];for(int i=0;i<6;i++)copy[i]=p[i]*weight*(i>=3?offsetScale:1);result.put(name,copy);});
            return new Pose(result);
        }
        public void apply(String name, ModelPart part) {
            float[] p = parts.get(name);
            if (p == null) return;
            part.xRot += p[0] * (float)Math.PI / 180; part.yRot += p[1] * (float)Math.PI / 180; part.zRot += p[2] * (float)Math.PI / 180;
            part.x += p[3]; part.y += p[4]; part.z += p[5];
        }
        /** Vanilla's adult tail segments are siblings, so carry the tip with its base. */
        public void applyFelineTail(ModelPart base, ModelPart tip, float length) {
            var before = new org.joml.Vector3f(0,length,0).rotateX(base.xRot).rotateY(base.yRot).rotateZ(base.zRot);
            float x=base.x,y=base.y,z=base.z;
            apply("tail1",base);
            if(length>0 && parts.containsKey("tail1")) {
                var after = new org.joml.Vector3f(0,length,0).rotateX(base.xRot).rotateY(base.yRot).rotateZ(base.zRot);
                tip.x+=base.x-x+after.x-before.x;tip.y+=base.y-y+after.y-before.y;tip.z+=base.z-z+after.z-before.z;
            }
            apply("tail2",tip);
        }
    }
    public static Pose sample(PetAction action, float elapsed) {
        return sample(action,elapsed,false);
    }
    public static Pose sample(PetAction action, float elapsed, boolean kittenRig) {
        return sample(action,elapsed,kittenRig,0);
    }
    public static Pose sample(PetAction action,float elapsed,boolean kittenRig,int variant) {
        if (action == PetAction.DOG_PET || !Float.isFinite(elapsed) || elapsed < 0 || elapsed >= action.duration) return Pose.NONE;
        String name = switch(action) { case CAT_PET -> "cat_pet"; case DIG -> "dog_dig"; case STRETCH -> kittenRig?"cat_stretch_baby":"cat_stretch";
            case KNEAD -> kittenRig?"cat_knead_baby":"cat_knead";case GROOM -> "cat_groom_chest"+(kittenRig?"_baby":"");case HEAD_TILT -> "dog_head_tilt";default -> "dog_pet"; };
        Clip clip = CACHE.computeIfAbsent(name, key->load(key,action));
        if (clip.frames.length < 2) return Pose.NONE;
        Frame a = clip.frames[0], b = clip.frames[clip.frames.length-1];
        for (int i=1; i<clip.frames.length; i++) if (elapsed <= clip.frames[i].tick) { a=clip.frames[i-1]; b=clip.frames[i]; break; }
        float t = Math.max(0, Math.min(1, (elapsed-a.tick)/(b.tick-a.tick))); t=t*t*(3-2*t);
        Map<String,float[]> result = new HashMap<>();
        Set<String> keys = new HashSet<>(a.parts.keySet()); keys.addAll(b.parts.keySet());
        for (String key : keys) {
            float[] left=a.parts.getOrDefault(key,ZERO), right=b.parts.getOrDefault(key,ZERO), value=new float[6];
            for (int i=0;i<6;i++) value[i]=left[i]+(right[i]-left[i])*t;
            result.put(key,value);
        }
        return new Pose(result);
    }
    private static Clip load(String name, PetAction action) {
        try (var reader = new InputStreamReader(Minecraft.getInstance().getResourceManager().open(SeamlessDogs.id("animations/"+name+".json")),StandardCharsets.UTF_8)) {
            Clip clip=JSON.fromJson(reader,Clip.class);
            if (clip == null || clip.frames == null || clip.frames.length < 2 || clip.frames.length > 128) throw new IllegalArgumentException("Invalid animation frames");
            int last=-1;
            for (Frame frame : clip.frames) {
                if (frame.tick<=last || frame.tick>action.duration || frame.parts==null || frame.parts.size()>12) throw new IllegalArgumentException("Invalid animation timing");
                last=frame.tick;
                for (float[] pose : frame.parts.values()) {
                    if (pose.length!=6) throw new IllegalArgumentException("Invalid bone transform");
                    for(float value:pose) if(!Float.isFinite(value)||Math.abs(value)>180) throw new IllegalArgumentException("Invalid animation value");
                }
            }
            return clip;
        } catch (Exception exception) {
            LoggerFactory.getLogger("SeamlessDogs").warn("Cannot load pet animation {}; retaining vanilla pose",name,exception);
            Clip empty=new Clip();empty.frames=new Frame[0];return empty;
        }
    }
    public static void clear() { CACHE.clear(); }
    private static final class Clip { Frame[] frames; }
    private static final class Frame { int tick; Map<String,float[]> parts; }
}
