package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
    private void seamlessdogs$pose(LivingEntity player,float limb,float amount,float age,float yaw,float pitch,CallbackInfo ci) {
        if (!ClientOptions.animation) return;
        var s=DogsClient.playerSample(player.getId(),age-player.tickCount);
        var model=(PlayerModel<?>)(Object)this;
        var local=net.minecraft.client.Minecraft.getInstance().player;
        if(DogsClient.bendToPet(player.getId())&&(local==null||player!=local||!net.minecraft.client.Minecraft.getInstance().options.getCameraType().isFirstPerson())){
            float bend=s.weight(),dy=12*(1-(float)Math.cos(bend)),dz=-12*(float)Math.sin(bend);
            model.body.xRot+=bend;model.body.y+=dy;model.body.z+=dz;model.head.y+=dy;model.head.z+=dz;
            model.leftArm.y+=dy;model.leftArm.z+=dz;model.rightArm.y+=dy;model.rightArm.z+=dz;model.hat.copyFrom(model.head);model.jacket.copyFrom(model.body);
        }
        boolean right=player.getMainArm()==HumanoidArm.RIGHT;
        var arm=right ? model.rightArm : model.leftArm;
        float sign=right?1:-1;
        arm.xRot+=(-1.15F-DogsClient.handHeightBias(player.getId())*.8F+0.15F*s.stroke())*s.weight();
        arm.yRot-=sign*0.25F*s.weight(); arm.zRot+=sign*0.12F*s.weight();
        // In 1.21.1 sleeves are sibling parts and must copy the final arm pose.
        model.rightSleeve.copyFrom(model.rightArm);model.leftSleeve.copyFrom(model.leftArm);
    }
}
