package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Unique private float[][] seamlessdogs$before, seamlessdogs$after;
    @Unique private ModelPart[] seamlessdogs$parts() {
        var model=(PlayerModel<?>)(Object)this;
        return new ModelPart[]{model.head,model.hat,model.body,model.jacket,model.leftArm,model.leftSleeve,model.rightArm,model.rightSleeve};
    }
    @Unique private static float[][] seamlessdogs$capture(ModelPart[] parts) {
        float[][] pose=new float[parts.length][];
        for(int i=0;i<parts.length;i++){var p=parts[i];pose[i]=new float[]{p.x,p.y,p.z,p.xRot,p.yRot,p.zRot};}
        return pose;
    }
    @Unique private static float seamlessdogs$undo(float current,float before,float after) {
        // Preserve an intervening renderer/mod reset instead of overwriting its pose.
        return Float.compare(current,after)==0?before:current;
    }
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("HEAD"))
    private void seamlessdogs$restore(LivingEntity player,float limb,float amount,float age,float yaw,float pitch,CallbackInfo ci) {
        if(seamlessdogs$after==null)return;
        var parts=seamlessdogs$parts();
        for(int i=0;i<parts.length;i++){
            var p=parts[i];var b=seamlessdogs$before[i];var a=seamlessdogs$after[i];
            p.x=seamlessdogs$undo(p.x,b[0],a[0]);p.y=seamlessdogs$undo(p.y,b[1],a[1]);p.z=seamlessdogs$undo(p.z,b[2],a[2]);
            p.xRot=seamlessdogs$undo(p.xRot,b[3],a[3]);p.yRot=seamlessdogs$undo(p.yRot,b[4],a[4]);p.zRot=seamlessdogs$undo(p.zRot,b[5],a[5]);
        }
        seamlessdogs$before=seamlessdogs$after=null;
    }
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"))
    private void seamlessdogs$pose(LivingEntity player,float limb,float amount,float age,float yaw,float pitch,CallbackInfo ci) {
        if (!ClientOptions.animation) return;
        var s=DogsClient.playerSample(player.getId(),age-player.tickCount);
        if(s.weight()<=0)return;
        var model=(PlayerModel<?>)(Object)this;
        var parts=seamlessdogs$parts();seamlessdogs$before=seamlessdogs$capture(parts);
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
        seamlessdogs$after=seamlessdogs$capture(parts);
    }
}
