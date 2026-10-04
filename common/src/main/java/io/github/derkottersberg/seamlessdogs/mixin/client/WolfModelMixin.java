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
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/WolfRenderState;)V", at = @At("TAIL"))
    private void seamlessdogs$relax(WolfRenderState state, CallbackInfo ci) {
        var sample = ((DogRenderData) state).seamlessdogs$sample();
        head.xRot += (-0.15F + 0.04F * sample.stroke()) * sample.weight();
        head.zRot += 0.13F * sample.weight();
        tail.yRot += (float) Math.sin(state.ageInTicks * 0.8F) * 0.6F * sample.weight();
    }
}
