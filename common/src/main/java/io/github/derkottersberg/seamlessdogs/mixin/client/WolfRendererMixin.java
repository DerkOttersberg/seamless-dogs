package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(WolfRenderer.class)
public abstract class WolfRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/wolf/Wolf;Lnet/minecraft/client/renderer/entity/state/WolfRenderState;F)V", at = @At("TAIL"))
    private void seamlessdogs$extract(Wolf dog, WolfRenderState state, float partial, CallbackInfo ci) {
        ((ActionRenderData)state).seamlessdogs$actionPose(ClientOptions.animation ? DogsClient.actionPose(dog.getUUID(),partial) : AnimationClips.Pose.NONE);
        var sample = ClientOptions.animation ? DogsClient.dogSample(dog.getUUID(), partial) : PetAnimation.sample(-1);
        ((DogRenderData) state).seamlessdogs$sample(sample);
        if (!ClientOptions.eyes || !dog.isTame() || dog.isAngry()) return;
        boolean happy = DogsClient.dogSample(dog.getUUID(), partial).weight() > 0.25F;
        int blink = Math.floorMod(dog.tickCount + dog.getUUID().hashCode(), 97);
        if (happy || blink < 3) state.texture = EyeTextures.expression(state.texture, happy, dog.isBaby());
    }
}
