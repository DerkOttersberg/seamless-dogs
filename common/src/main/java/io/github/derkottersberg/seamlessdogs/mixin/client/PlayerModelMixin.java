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
        var model=(PlayerModel)(Object)this;
        var local=net.minecraft.client.Minecraft.getInstance().player;
        boolean worldPose=local==null||state.id!=local.getId()||!net.minecraft.client.Minecraft.getInstance().options.getCameraType().isFirstPerson();
        if(worldPose&&DogsClient.bendToPet(state.id)) {
            float bend=sample.weight(),dy=12*(1-(float)Math.cos(bend)),dz=-12*(float)Math.sin(bend);
            // Bend from the hips so shoulders and hands reach a cat without moving the player.
            model.body.xRot+=bend;model.body.y+=dy;model.body.z+=dz;
            model.head.y+=dy;model.head.z+=dz;
            model.leftArm.y+=dy;model.leftArm.z+=dz;model.rightArm.y+=dy;model.rightArm.z+=dz;
        }
        var arm = model.getArm(state.mainArm);
        float sign = state.mainArm == HumanoidArm.RIGHT ? 1 : -1;
        arm.xRot += (-1.15F - DogsClient.handHeightBias(state.id)*.8F + 0.15F * sample.stroke()) * sample.weight();
        arm.yRot += -sign * 0.25F * sample.weight();
        arm.zRot += sign * 0.12F * sample.weight();
        // Skin sleeves are children in 26.3 and inherit the arm transform once.
    }
}
