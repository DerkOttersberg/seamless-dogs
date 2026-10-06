package qa.dogs.mixin;
import qa.dogs.ClientProbe;
import io.github.derkottersberg.seamlessdogs.client.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
public abstract class RenderProbe {
    @Shadow protected EntityModel<?> model;
    @Inject(method="render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at=@At(value="INVOKE",target="Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",shift=At.Shift.AFTER))
    private void qa$completedPose(LivingEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light,CallbackInfo ci) {
        if(entity instanceof Wolf wolf) {
            var texture=((WolfRenderer)(Object)this).getTextureLocation(wolf);
            if(texture.getNamespace().equals("seamlessdogs") && texture.getPath().endsWith("_blink")) ClientProbe.blinks++;
            var sample=DogsClient.dogSample(wolf.getUUID(),partial);
            if(sample.weight()>0.2F) {
                var rig=(WolfProbe)model;
                if(!Float.isFinite(rig.qa$tail().yRot) || !Float.isFinite(rig.qa$head().zRot) || Math.abs(rig.qa$head().zRot)<0.01F)
                    throw new IllegalStateException("Invalid dog rig");
                ClientProbe.wolves++;
                if(texture.getNamespace().equals("seamlessdogs")) ClientProbe.expressiveEyes++;
            }
        }
        if(model instanceof PlayerModel<?> player) {
            var sample=DogsClient.playerSample(entity.getId(),partial);
            if(sample.weight()>0.2F) {
                boolean right=entity.getMainArm()==HumanoidArm.RIGHT;
                var arm=right?player.rightArm:player.leftArm;
                var sleeve=right?player.rightSleeve:player.leftSleeve;
                if(!Float.isFinite(arm.xRot) || Math.abs(arm.xRot)<0.1F || Math.abs(sleeve.xRot-arm.xRot)>0.001F)
                    throw new IllegalStateException("Invalid player arm/sleeve");
                ClientProbe.playerModels++;
            }
        }
    }
}
