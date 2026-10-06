package qa.dogs.mixin;
import qa.dogs.ClientProbe;
import io.github.derkottersberg.seamlessdogs.client.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
public abstract class RenderProbe {
    @Shadow protected EntityModel<?> model;
    @Inject(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at=@At(value="INVOKE",target="Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",shift=At.Shift.AFTER))
    private void qa$completedPose(LivingEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light,CallbackInfo ci) {
        if(entity instanceof net.minecraft.world.entity.animal.Cat || entity instanceof Wolf)qa.dogs.ObserverProbe.rendered(entity.getUUID());
        if(entity instanceof net.minecraft.world.entity.animal.Cat cat){
            qa.dogs.PetFeatureProbe.catFrames++;
            var texture=((net.minecraft.client.renderer.entity.CatRenderer)(Object)this).getTextureLocation(cat);
            if(texture.getNamespace().equals("seamlessdogs"))qa.dogs.PetFeatureProbe.catEyes++;
            var action=DogsClient.actionPose(cat.getUUID(),partial);
            if(!action.parts().isEmpty()){
                var rig=(io.github.derkottersberg.seamlessdogs.mixin.client.FelineParts)model;
                if(!Float.isFinite(rig.seamlessdogs$head().xRot)||!Float.isFinite(rig.seamlessdogs$leftFront().xRot))throw new IllegalStateException("Invalid native cat pose");
                qa.dogs.PetFeatureProbe.catPoses++;
                Integer expected=qa.dogs.PetFeatureProbe.expectedGrooms.get(cat.getUUID());
                var align=action.parts().get("head_alignment");
                if(expected!=null&&align!=null&&align[0]>.1F){
                    boolean left=action.parts().containsKey("left_front_leg"),right=action.parts().containsKey("right_front_leg");
                    int rendered=right&&!left?0:left&&!right?1:!left&&!right?2:-1;
                    if(rendered!=expected)throw new IllegalStateException("Wrong rendered grooming variant");
                    qa.dogs.PetFeatureProbe.renderedGroomVariants|=1<<rendered;
                }
                var body=action.parts().get("body");
                if(body!=null&&body[0]>3){
                    var front=rig.seamlessdogs$leftFront();
                    double paw=front.y+10*Math.cos(front.xRot);
                    if(Math.abs(paw-24)>.65)throw new IllegalStateException("Scaled adult/kitten stretch paw floats: "+paw);
                    for(var hind:new net.minecraft.client.model.geom.ModelPart[]{rig.seamlessdogs$leftHind(),rig.seamlessdogs$rightHind()}){
                        double bottom=hind.y+6*Math.cos(hind.xRot)-2*Math.sin(hind.xRot);
                        if(Math.abs(bottom-24)>.25)throw new IllegalStateException("Stretch hind paw floats: "+bottom);
                    }
                }
            }
        }
        if(entity instanceof Wolf wolf) {
            var action=DogsClient.actionPose(wolf.getUUID(),partial);
            if(action.parts().containsKey("left_ear")){
                var real=((WolfProbe)model).qa$head().getChild("real_head");
                var ear=real.getChild("seamlessdogs_left_ear");
                float expected=action.parts().get("left_ear")[2]*(float)Math.PI/180;
                if(!Float.isFinite(ear.zRot)||Math.abs(ear.zRot-ear.getInitialPose().zRot-expected)>.001)throw new IllegalStateException("Native ear fold missing");
            }
            if(!DogsClient.actionPose(wolf.getUUID(),partial).parts().isEmpty())qa.dogs.PetFeatureProbe.digPoses++;
            var texture=((WolfRenderer)(Object)this).getTextureLocation(wolf);
            if(texture.getNamespace().equals("seamlessdogs") && texture.getPath().endsWith("_blink")) ClientProbe.blinks++;
            var sample=DogsClient.dogSample(wolf.getUUID(),partial);
            if(sample.weight()>0.2F) {
                var rig=(WolfProbe)model;
                if(!Float.isFinite(rig.qa$tail().yRot) || !Float.isFinite(rig.qa$head().zRot) || Math.abs(rig.qa$head().zRot)<0.01F)
                    throw new IllegalStateException("Invalid dog rig");
                ClientProbe.wolves++;
                if(texture.getNamespace().equals("seamlessdogs")) ClientProbe.expressiveEyes++;
            }
        }
        if(model instanceof PlayerModel<?> player) {
            var sample=DogsClient.playerSample(entity.getId(),partial);
            for(var part:new net.minecraft.client.model.geom.ModelPart[]{player.head,player.hat,player.body,player.jacket,player.leftArm,player.leftSleeve,player.rightArm,player.rightSleeve})
                if(!Float.isFinite(part.y)||!Float.isFinite(part.z)||Math.abs(part.y)>20||Math.abs(part.z)>20)
                    throw new IllegalStateException("Accumulating native player pose: y="+part.y+" z="+part.z);
            if(sample.weight()>0.2F&&DogsClient.bendToPet(entity.getId()))qa.dogs.SkinProbe.playerPoseChecks++;
            if(sample.weight()>0.2F) {
                boolean right=entity.getMainArm()==HumanoidArm.RIGHT;
                var arm=right?player.rightArm:player.leftArm;
                var sleeve=right?player.rightSleeve:player.leftSleeve;
                if(!Float.isFinite(arm.xRot) || Math.abs(arm.xRot)<0.1F || Math.abs(sleeve.xRot-arm.xRot)>0.001F)
                    throw new IllegalStateException("Invalid player arm/sleeve");
                qa.dogs.SkinProbe.record(((net.minecraft.client.player.AbstractClientPlayer)entity).getModelName());
                ClientProbe.playerModels++;
            }
        }
    }
}
