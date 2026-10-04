package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(WolfRenderer.class)
public abstract class WolfRendererMixin {
    @Inject(method="getTextureLocation(Lnet/minecraft/world/entity/animal/Wolf;)Lnet/minecraft/resources/ResourceLocation;",at=@At("RETURN"),cancellable=true)
    private void seamlessdogs$eyes(Wolf dog, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!ClientOptions.eyes || !dog.isTame() || dog.isAngry()) return;
        boolean happy= DogsClient.dogSample(dog.getUUID(), 0).weight() > 0.25F;
        if (happy || Math.floorMod(dog.tickCount + dog.getUUID().hashCode(),97)<3)
            cir.setReturnValue(EyeTextures.expression(cir.getReturnValue(),happy));
    }
}
