package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.model.animal.feline.*;
import net.minecraft.client.renderer.entity.state.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin({AdultFelineModel.class,BabyFelineModel.class})
public abstract class CatModelMixin {
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/FelineRenderState;)V",at=@At("TAIL"))
    private void seamlessdogs$pose(FelineRenderState state,CallbackInfo ci) {
        if (!(state instanceof CatRenderState) || (((Object)this).getClass()!=AdultCatModel.class && ((Object)this).getClass()!=BabyCatModel.class)) return;
        var pose=((ActionRenderData)state).seamlessdogs$actionPose(); var parts=(FelineParts)this;
        // Blend away ambient looking during the authored idle clip; restore vanilla naturally on recovery.
        var align=pose.parts().get("head_alignment");
        if(align!=null){float keep=1-Math.max(0,Math.min(1,align[0]));parts.seamlessdogs$head().xRot*=keep;parts.seamlessdogs$head().yRot*=keep;parts.seamlessdogs$head().zRot*=keep;}
        pose.apply("head",parts.seamlessdogs$head()); pose.apply("body",parts.seamlessdogs$body());
        pose.applyFelineTail(parts.seamlessdogs$tail1(),parts.seamlessdogs$tail2(),state.isBaby?0:8);
        pose.apply("left_front_leg",parts.seamlessdogs$leftFront()); pose.apply("right_front_leg",parts.seamlessdogs$rightFront());
        pose.apply("left_hind_leg",parts.seamlessdogs$leftHind()); pose.apply("right_hind_leg",parts.seamlessdogs$rightHind());
    }
}
