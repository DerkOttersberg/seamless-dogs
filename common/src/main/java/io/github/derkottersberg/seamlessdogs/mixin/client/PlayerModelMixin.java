package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void seamlessdogs$pet(AvatarRenderState state, CallbackInfo ci) {
        if (!ClientOptions.animation || state.isSpectator) return;
        var sample = DogsClient.playerSample(state.id, state.ageInTicks % 1);
        var arm = ((PlayerModel) (Object) this).getArm(state.mainArm);
        float sign = state.mainArm == HumanoidArm.RIGHT ? 1 : -1;
        arm.xRot += (-1.15F + 0.15F * sample.stroke()) * sample.weight();
        arm.yRot += -sign * 0.25F * sample.weight();
        arm.zRot += sign * 0.12F * sample.weight();
        // Skin sleeves are children in 26.3 and inherit the arm transform once.
    }
}
