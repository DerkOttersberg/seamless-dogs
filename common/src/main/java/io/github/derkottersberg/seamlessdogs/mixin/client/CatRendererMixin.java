package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(CatRenderer.class)
public abstract class CatRendererMixin {
 @Inject(method="getTextureLocation(Lnet/minecraft/world/entity/animal/Cat;)Lnet/minecraft/resources/ResourceLocation;",at=@At("RETURN"),cancellable=true)
 private void seamlessdogs$eyes(Cat cat,CallbackInfoReturnable<ResourceLocation> cir){
  if(!ClientOptions.eyes||!cat.isTame()||cat.getTarget()!=null)return;
  boolean happy=DogsClient.petSample(cat.getUUID(),0).weight()>.25F;
  int slow=Math.floorMod(cat.tickCount+cat.getUUID().hashCode(),28),blink=Math.floorMod(cat.tickCount+cat.getUUID().hashCode(),137);
  var original=cir.getReturnValue();
  if(DogsClient.stretchingEyes(cat.getUUID(),0))cir.setReturnValue(EyeTextures.catExpression(original,true,false));
  else if(happy)cir.setReturnValue(slow<8?EyeTextures.catExpression(original,true,false):EyeTextures.catRelaxed(original,false));
  else if(blink<4)cir.setReturnValue(EyeTextures.catExpression(original,false,false));
 }
}
