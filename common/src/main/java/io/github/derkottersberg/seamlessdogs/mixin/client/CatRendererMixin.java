package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.world.entity.animal.feline.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CatRenderer.class)
public abstract class CatRendererMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/animal/feline/Cat;Lnet/minecraft/client/renderer/entity/state/CatRenderState;F)V",at=@At("TAIL"))
    private void seamlessdogs$extract(Cat cat,CatRenderState state,float partial,CallbackInfo ci) {
        ((ActionRenderData)state).seamlessdogs$actionPose(ClientOptions.animation ? DogsClient.actionPose(cat.getUUID(),partial) : AnimationClips.Pose.NONE);
        if (!ClientOptions.eyes || !cat.isTame() || cat.getTarget()!=null) return;
        boolean happy=DogsClient.petSample(cat.getUUID(),partial).weight()>.25F;
        int blink=Math.floorMod(cat.tickCount+cat.getUUID().hashCode(),137);
        int slowBlink=Math.floorMod(cat.tickCount+cat.getUUID().hashCode(),28);
        if (DogsClient.stretchingEyes(cat.getUUID(),partial)) state.texture=EyeTextures.catExpression(state.texture,true,cat.isBaby());
        else if (happy) state.texture=slowBlink<8 ? EyeTextures.catExpression(state.texture,true,cat.isBaby()) : EyeTextures.catRelaxed(state.texture,cat.isBaby());
        else if (blink<4) state.texture=EyeTextures.catExpression(state.texture,false,cat.isBaby());
    }
}
