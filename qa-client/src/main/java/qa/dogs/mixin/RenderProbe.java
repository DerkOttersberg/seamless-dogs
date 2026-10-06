package qa.dogs.mixin;
import qa.dogs.ClientProbe;
import io.github.derkottersberg.seamlessdogs.client.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
public abstract class RenderProbe {
    @Shadow protected EntityModel<?> model;
    @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at=@At(value="INVOKE",target="Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V",shift=At.Shift.AFTER))
    private void qa$completedPose(LivingEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if(state instanceof CatRenderState || state instanceof WolfRenderState)qa.dogs.ObserverProbe.renderedAt(state.x,state.y,state.z);
        if(state instanceof CatRenderState && !((ActionRenderData)state).seamlessdogs$actionPose().parts().isEmpty()) {
            var rig=(io.github.derkottersberg.seamlessdogs.mixin.client.FelineParts)model;
            if(!Float.isFinite(rig.seamlessdogs$head().xRot)||!Float.isFinite(rig.seamlessdogs$body().y))throw new IllegalStateException("Nonfinite cat pose");
            qa.dogs.PetFeatureProbe.catPoses++;
            var body=((ActionRenderData)state).seamlessdogs$actionPose().parts().get("body");
            if(body!=null&&body[0]>3) {
                var front=rig.seamlessdogs$leftFront();
                float length=state.isBaby?2:10;
                double bottom=front.y+length*Math.cos(front.xRot);
                if(Math.abs(bottom-24)>.65)throw new IllegalStateException("Stretch paw leaves ground: "+bottom+" baby="+state.isBaby);
                if(!state.isBaby)for(var hind:new net.minecraft.client.model.geom.ModelPart[]{rig.seamlessdogs$leftHind(),rig.seamlessdogs$rightHind()}) {
                    double paw=hind.y+6*Math.cos(hind.xRot)-2*Math.sin(hind.xRot);
                    if(Math.abs(paw-24)>.25)throw new IllegalStateException("Adult stretch hind paw floats: "+paw);
                }
            }
        }
        if (state instanceof WolfRenderState wolf) {
            var actionPose=((ActionRenderData)state).seamlessdogs$actionPose();
            if(actionPose.parts().containsKey("left_ear")){
                var head=((WolfProbe)model).qa$head();var ears=head.hasChild("real_head")?head.getChild("real_head"):head;
                var ear=ears.getChild((state.isBaby?"":"seamlessdogs_")+"left_ear");
                float expected=actionPose.parts().get("left_ear")[2]*(float)Math.PI/180;
                if(!Float.isFinite(ear.zRot)||Math.abs(ear.zRot-ear.getInitialPose().zRot()-expected)>.001)throw new IllegalStateException("Native ear fold missing");
            }
            if(!((ActionRenderData)state).seamlessdogs$actionPose().parts().isEmpty())qa.dogs.PetFeatureProbe.digPoses++;
            if (wolf.texture.getNamespace().equals("seamlessdogs") && wolf.texture.getPath().endsWith("_blink")) ClientProbe.blinks++;
            var sample = ((DogRenderData) wolf).seamlessdogs$sample();
            if (sample.weight() > 0.2F) {
                var rig = (WolfProbe) model;
                if (!Float.isFinite(rig.qa$tail().yRot) || !Float.isFinite(rig.qa$head().zRot) || Math.abs(rig.qa$head().zRot) < 0.01F)
                    throw new IllegalStateException("Dog final pose incorrect head=" + rig.qa$head().zRot + " weight=" + sample.weight());
                ClientProbe.wolves++;
                if (wolf.texture.getNamespace().equals("seamlessdogs")) ClientProbe.expressiveEyes++;
            }
        }
        if (state instanceof AvatarRenderState avatar && model instanceof PlayerModel player) {
            var sample = DogsClient.playerSample(avatar.id, avatar.ageInTicks % 1);
            if (sample.weight() > 0.2F) {
                if (!Float.isFinite(player.getArm(avatar.mainArm).xRot) || Math.abs(player.getArm(avatar.mainArm).xRot) < 0.1F || player.leftSleeve.xRot != 0 || player.rightSleeve.xRot != 0)
                    throw new IllegalStateException("Invalid final arm/sleeve transform");
                qa.dogs.SkinProbe.record(avatar.skin.model().name());
                ClientProbe.playerModels++;
            }
        }
    }
}
