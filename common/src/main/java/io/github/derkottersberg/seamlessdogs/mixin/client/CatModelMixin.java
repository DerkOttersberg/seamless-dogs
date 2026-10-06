package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.model.CatModel;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CatModel.class)
public abstract class CatModelMixin {
 @Inject(method="prepareMobModel(Lnet/minecraft/world/entity/animal/Cat;FFF)V",at=@At("HEAD"))
 private void seamlessdogs$reset(Cat cat,float limb,float amount,float partial,CallbackInfo ci){
  if(((Object)this).getClass()!=CatModel.class)return;
  var parts=(FelineParts)this;
  // These Minecraft lines do not reset every bone between entities/frames.
  // Reset before vanilla prepares walking, sitting and sleeping poses.
  for(var part:new net.minecraft.client.model.geom.ModelPart[]{parts.seamlessdogs$head(),parts.seamlessdogs$body(),parts.seamlessdogs$tail1(),parts.seamlessdogs$tail2(),parts.seamlessdogs$leftFront(),parts.seamlessdogs$rightFront(),parts.seamlessdogs$leftHind(),parts.seamlessdogs$rightHind()})part.resetPose();
 }

 @Inject(method="setupAnim(Lnet/minecraft/world/entity/animal/Cat;FFFFF)V",at=@At("TAIL"))
 private void seamlessdogs$pose(Cat cat,float limb,float amount,float age,float yaw,float pitch,CallbackInfo ci){
  if(!ClientOptions.animation||((Object)this).getClass()!=CatModel.class)return;
  var pose=DogsClient.actionPose(cat.getUUID(),age-cat.tickCount);var parts=(FelineParts)this;
  var align=pose.parts().get("head_alignment");
  if(align!=null){float keep=1-Math.max(0,Math.min(1,align[0]));parts.seamlessdogs$head().xRot*=keep;parts.seamlessdogs$head().yRot*=keep;parts.seamlessdogs$head().zRot*=keep;}
  pose.apply("head",parts.seamlessdogs$head());pose.apply("body",parts.seamlessdogs$body());
  pose.applyFelineTail(parts.seamlessdogs$tail1(),parts.seamlessdogs$tail2(),8);
  pose.apply("left_front_leg",parts.seamlessdogs$leftFront());pose.apply("right_front_leg",parts.seamlessdogs$rightFront());
  pose.apply("left_hind_leg",parts.seamlessdogs$leftHind());pose.apply("right_hind_leg",parts.seamlessdogs$rightHind());
 }
}
