package io.github.derkottersberg.seamlessdogs.mixin.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandRenderer.class)
public abstract class HandsMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void seamlessdogs$pet(AbstractClientPlayer player, float partial,
        float pitch, InteractionHand hand, float swing, ItemStack item, float equip, PoseStack pose, MultiBufferSource collector, int light, CallbackInfo ci) {
        var local = Minecraft.getInstance().player;
        if (!ClientOptions.animation || local == null || hand != InteractionHand.MAIN_HAND || !item.isEmpty()
            || player.isInvisible()) return;
        var sample = DogsClient.playerSample(local.getId(), partial);
        if (sample.weight() <= 0.001F) return;
        var arm = player.getMainArm();
        float sign = arm == HumanoidArm.RIGHT ? 1 : -1;
        pose.pushPose();
        pose.translate(-sign * 0.50F * sample.weight(), (0.60F + 0.045F * sample.stroke()) * sample.weight(), -0.10F * sample.weight());
        pose.mulPose(Axis.XP.rotationDegrees(-10 * sample.weight()));
        pose.mulPose(Axis.YP.rotationDegrees(sign * 16 * sample.weight()));
        pose.mulPose(Axis.ZP.rotationDegrees(-sign * 12 * sample.weight()));
        ((HandsInvoker) this).seamlessdogs$arm(pose, collector, light, equip, 0, arm);
        pose.popPose();
        ci.cancel();
    }
}
