package qa.dogs;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.derkottersberg.seamlessdogs.client.DogsClient;
import java.nio.file.Files;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;

/** Measures the actual packaged arm invocation relative to its vanilla camera pose. */
public final class HandReturnProbe {
    private static Matrix4f baseline;
    private static float partial,originalSwing;
    private static String label;
    private static boolean interrupted,peak,returning,complete;
    private static float previousWeight,previousDistance,frozenStroke;
    private static int frames,lastCapture;
    private static String pendingCapture;
    private static final List<String> rows=new ArrayList<>(),passed=new ArrayList<>();
    public static void begin(PoseStack pose,float fraction,float swing) {
        baseline=new Matrix4f(pose.last().pose()).invert();partial=fraction;originalSwing=swing;
    }
    public static void start(String name,boolean cancellation) {
        label=name;interrupted=cancellation;peak=returning=complete=false;
        previousWeight=0;previousDistance=Float.MAX_VALUE;frames=0;lastCapture=-1;
    }
    public static void arm(PoseStack pose,float swing,HumanoidArm arm) {
        var c=Minecraft.getInstance();if(c.player==null||label==null||baseline==null)return;
        var sample=DogsClient.playerSample(c.player.getId(),partial);
        var delta=new Matrix4f(baseline).mul(pose.last().pose());
        float[] matrix=delta.get(new float[16]);float distance=0;
        for(int i=0;i<16;i++){float v=matrix[i]-(i%5==0?1:0);distance+=v*v;}
        distance=(float)Math.sqrt(distance);
        String row=label+","+c.level.getGameTime()+","+c.player.tickCount+","+partial+","+arm+","+sample.weight()+","+sample.stroke()+","+distance+","+originalSwing+","+swing;
        rows.add(row);
        peak|=sample.weight()>.8F;
        if(peak&&sample.weight()<previousWeight-.00001F&&!returning){returning=true;frozenStroke=sample.stroke();}
        if(returning&&!complete) {
            if(distance>previousDistance+.0002F) {
                try{Files.writeString(c.gameDirectory.toPath().resolve("hand-return-failed-frames.csv"),String.join("\n",rows));}catch(Exception ignored){}
                throw new IllegalStateException("Hand reverses during return: "+row+" previous="+previousDistance+" recent="+rows.subList(Math.max(0,rows.size()-6),rows.size()));
            }
            if(interrupted&&sample.weight()>0&&Math.abs(sample.stroke()-frozenStroke)>.00001F)throw new IllegalStateException("Stopped hand replays a partial tick: "+label);
            if(!interrupted&&Math.abs(sample.stroke())>.00001F)throw new IllegalStateException("Stroke continues during withdrawal: "+label);
            if(sample.weight()==0&&distance>.00002F)throw new IllegalStateException("Hand does not meet the vanilla pose: "+label);
            if(originalSwing>0&&sample.weight()<.8F&&swing<=0)throw new IllegalStateException("Vanilla swing snaps back only at clip end: "+label);
            frames++;previousDistance=distance;
            int tick=c.player.tickCount;
            // Arm submission happens before its draw pass. Read the target
            // after the complete frame, otherwise captures omit the hand.
            // Full gameplay runs capture the settled pose only: synchronous
            // software-GPU readback must not skip intermediate matrix samples.
            if(tick!=lastCapture&&(Boolean.getBoolean("qa.handOnly")||sample.weight()==0)){lastCapture=tick;pendingCapture="hand-return-"+label+"-"+frames+".png";}
            if(sample.weight()==0) {complete=true;passed.add(label+" frames="+frames);ClientProbe.log("PASS continuous hand return "+label+" frames="+frames);}
        }
        previousWeight=sample.weight();
    }
    public static void captureAfterFrame(Minecraft c) {
        if(pendingCapture==null)return;
        String name=pendingCapture;pendingCapture=null;
        Screenshot.grab(c.gameDirectory,name,c.getMainRenderTarget(),m->{});
    }
    public static void requireComplete() {
        if(!complete||frames<4)throw new IllegalStateException("Hand return not captured: "+label+" frames="+frames);
    }
    public static void finish(Minecraft c) {
        if(passed.size()!=4)throw new IllegalStateException("Missing return cases "+passed);
        try {
            Files.writeString(c.gameDirectory.toPath().resolve("hand-return-passed.txt"),"PASS actual right/left arm matrices: natural return, server interruption, fractional-tick continuity, vanilla swing blending and zero residual pose.\n"+String.join("\n",passed)+"\n");
            Files.writeString(c.gameDirectory.toPath().resolve("hand-return-frames.csv"),"case,worldTick,clientTick,partial,arm,weight,stroke,poseDistance,originalSwing,renderedSwing\n"+String.join("\n",rows)+"\n");
        } catch(Exception e){throw new RuntimeException(e);}
        label=null;
    }
}
