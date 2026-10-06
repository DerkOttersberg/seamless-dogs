package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.DogRenderData;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(WolfModel.class)
public abstract class WolfModelMixin {
    @Shadow @Final protected ModelPart head;
    @Shadow @Final protected ModelPart tail;
    @Shadow @Final protected ModelPart body,leftFrontLeg,rightFrontLeg,leftHindLeg,rightHindLeg;
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/WolfRenderState;)V", at = @At("TAIL"))
    private void seamlessdogs$relax(WolfRenderState state, CallbackInfo ci) {
        if(((Object)this).getClass()!=net.minecraft.client.model.animal.wolf.AdultWolfModel.class
            && ((Object)this).getClass()!=net.minecraft.client.model.animal.wolf.BabyWolfModel.class)return;
        var pose=((io.github.derkottersberg.seamlessdogs.client.ActionRenderData)state).seamlessdogs$actionPose();
        var align=pose.parts().get("head_alignment");
        if(align!=null){float keep=1-Math.max(0,Math.min(1,align[0]));head.xRot*=keep;head.yRot*=keep;head.zRot*=keep;}
        var root=((WolfModel)(Object)this).root();
        if(root.hasChild("upper_body"))pose.apply("upper_body",root.getChild("upper_body"));
        pose.apply("head",head);pose.apply("body",body);pose.apply("tail",tail);
        pose.apply("left_front_leg",leftFrontLeg);pose.apply("right_front_leg",rightFrontLeg);
        pose.apply("left_hind_leg",leftHindLeg);pose.apply("right_hind_leg",rightHindLeg);
        var ears=head.hasChild("real_head")?head.getChild("real_head"):head;
        String prefix=state.isBaby?"":"seamlessdogs_";
        for(String side:new String[]{"left","right"})if(ears.hasChild(prefix+side+"_ear"))pose.apply(side+"_ear",ears.getChild(prefix+side+"_ear"));
        var sample = ((DogRenderData) state).seamlessdogs$sample();
        head.xRot += (-0.15F + 0.04F * sample.stroke()) * sample.weight();
        head.zRot += 0.13F * sample.weight();
        tail.yRot += (float) Math.sin(state.ageInTicks * 0.8F) * 0.6F * sample.weight();
    }
}
