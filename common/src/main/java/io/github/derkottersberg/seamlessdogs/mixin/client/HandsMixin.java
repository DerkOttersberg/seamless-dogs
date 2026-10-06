package io.github.derkottersberg.seamlessdogs.mixin.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class HandsMixin {
    @ModifyArgs(method = "submitArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FirstPersonHandsAndItemsRenderer;renderPlayerArm(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IFFLnet/minecraft/world/entity/HumanoidArm;Lnet/minecraft/client/renderer/state/level/PlayerRenderState;)V"))
    private void seamlessdogs$pet(Args args, PlayerRenderState player, FirstPersonHandsAndItemsRenderState hands, float partial,
        float pitch, InteractionHand hand, float swing, ItemStack item, float equip, PoseStack pose, SubmitNodeCollector collector, int light) {
        var local = Minecraft.getInstance().player;
        if (!ClientOptions.animation || local == null || hand != InteractionHand.MAIN_HAND || !item.isEmpty()
            || !player.hasPlayer || player.avatarRenderState.isInvisible) return;
        var sample = DogsClient.playerSample(local.getId(), partial);
        if (sample.weight() <= 0) return;
        HumanoidArm arm = args.get(5);
        float sign = arm == HumanoidArm.RIGHT ? 1 : -1;
        pose.translate(-sign * 0.42F * sample.weight(), (0.40F + DogsClient.handHeightBias(local.getId())*.35F + 0.025F * sample.stroke()) * sample.weight(), -0.18F * sample.weight());
        pose.rotate(Axis.XP.rotationDegrees(-12 * sample.weight()));
        pose.rotate(Axis.YP.rotationDegrees(sign * 6 * sample.weight()));
        pose.rotate(Axis.ZP.rotationDegrees(sign * 8 * sample.weight()));
        args.set(4, PetAnimation.vanillaSwing(swing, sample.weight()));
    }
}
