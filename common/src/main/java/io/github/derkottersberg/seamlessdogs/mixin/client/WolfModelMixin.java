package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(WolfModel.class)
public abstract class WolfModelMixin {
    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart tail;
    @Shadow @Final private ModelPart upperBody;
    @Shadow @Final private ModelPart realHead;
    @Shadow @Final private ModelPart body,leftFrontLeg,rightFrontLeg,leftHindLeg,rightHindLeg;
    @Unique private float seamlessdogs$renderPartial;
    // WolfRenderer uses the setupAnim age argument for the tail angle.
    // Capture the real frame delta from prepareMobModel instead.
    @Inject(method="prepareMobModel(Lnet/minecraft/world/entity/animal/Wolf;FFF)V",at=@At("HEAD"))
    private void seamlessdogs$frame(Wolf dog,float limb,float amount,float partial,CallbackInfo ci) {
        seamlessdogs$renderPartial=partial;
        if(((Object)this).getClass()==WolfModel.class)
            for(var part:new ModelPart[]{head,body,upperBody,tail,leftFrontLeg,rightFrontLeg,leftHindLeg,rightHindLeg})part.resetPose();
    }
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/animal/Wolf;FFFFF)V",at=@At("TAIL"))
    private void seamlessdogs$pose(Wolf dog,float limb,float amount,float age,float yaw,float pitch,CallbackInfo ci) {
        for(String side:new String[]{"left","right"})if(realHead.hasChild("seamlessdogs_"+side+"_ear"))realHead.getChild("seamlessdogs_"+side+"_ear").resetPose();
        if(ClientOptions.animation&&((Object)this).getClass()==WolfModel.class){
            var pose=DogsClient.actionPose(dog.getUUID(),seamlessdogs$renderPartial);
            var align=pose.parts().get("head_alignment");
            if(align!=null){float keep=1-Math.max(0,Math.min(1,align[0]));head.xRot*=keep;head.yRot*=keep;head.zRot*=keep;}
            pose.apply("upper_body",upperBody);
            for(String side:new String[]{"left","right"})if(realHead.hasChild("seamlessdogs_"+side+"_ear"))pose.apply(side+"_ear",realHead.getChild("seamlessdogs_"+side+"_ear"));
            pose.apply("head",head);pose.apply("body",body);pose.apply("tail",tail);
            pose.apply("left_front_leg",leftFrontLeg);pose.apply("right_front_leg",rightFrontLeg);
            pose.apply("left_hind_leg",leftHindLeg);pose.apply("right_hind_leg",rightHindLeg);
        }
        var s=ClientOptions.animation&&((Object)this).getClass()==WolfModel.class ? DogsClient.dogSample(dog.getUUID(),seamlessdogs$renderPartial) : PetAnimation.sample(-1);
        head.xRot+=(-0.15F+0.04F*s.stroke())*s.weight();
        head.zRot+=0.13F*s.weight();
        tail.yRot+=(float)Math.sin((dog.tickCount+seamlessdogs$renderPartial)*0.8F)*0.6F*s.weight();
    }
}
