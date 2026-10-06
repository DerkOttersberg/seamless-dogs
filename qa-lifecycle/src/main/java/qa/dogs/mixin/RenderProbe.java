package qa.dogs.mixin;
import qa.dogs.ClientProbe;
import io.github.derkottersberg.seamlessdogs.client.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
public abstract class RenderProbe {
    @Shadow protected EntityModel<?> model;
    @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at=@At(value="INVOKE",target="Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V",shift=At.Shift.AFTER))
    private void qa$completedPose(LivingEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (state instanceof WolfRenderState wolf) {
            if (wolf.texture.getNamespace().equals("seamlessdogs") && wolf.texture.getPath().endsWith("_blink")) ClientProbe.blinks++;
            var sample = ((DogRenderData) wolf).seamlessdogs$sample();
            if (sample.weight() > 0.2F) {
                var rig = (WolfProbe) model;
                if (!Float.isFinite(rig.qa$tail().yRot) || !Float.isFinite(rig.qa$head().zRot) || Math.abs(rig.qa$head().zRot) < 0.01F)
                    throw new IllegalStateException("Dog final pose incorrect head=" + rig.qa$head().zRot + " weight=" + sample.weight());
                ClientProbe.wolves++;
                if (wolf.texture.getNamespace().equals("seamlessdogs")) ClientProbe.expressiveEyes++;
            }
        }
        if (state instanceof AvatarRenderState avatar && model instanceof PlayerModel player) {
            var sample = DogsClient.playerSample(avatar.id, avatar.ageInTicks % 1);
            if (sample.weight() > 0.2F) {
                if (!Float.isFinite(player.getArm(avatar.mainArm).xRot) || Math.abs(player.getArm(avatar.mainArm).xRot) < 0.1F || player.leftSleeve.xRot != 0 || player.rightSleeve.xRot != 0)
                    throw new IllegalStateException("Invalid final arm/sleeve transform");
                ClientProbe.playerModels++;
            }
        }
    }
}
