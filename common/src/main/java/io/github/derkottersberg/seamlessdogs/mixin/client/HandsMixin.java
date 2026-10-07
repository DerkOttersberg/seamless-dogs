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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandRenderer.class)
public abstract class HandsMixin {
    @Unique private float seamlessdogs$handWeight;
    @Unique private float seamlessdogs$handStroke;
    @Unique private float seamlessdogs$handHeight;

    // Sample once before argument modifiers run; each arm draw starts with vanilla state.
    @Inject(method="renderArmWithItem",at=@At("HEAD"))
    private void seamlessdogs$sample(AbstractClientPlayer player, float partial,
        float pitch, InteractionHand hand, float swing, ItemStack item, float equip, PoseStack pose, MultiBufferSource collector, int light, CallbackInfo ci) {
        seamlessdogs$handWeight=0;
        var local=Minecraft.getInstance().player;
        if(!ClientOptions.animation||local==null||hand!=InteractionHand.MAIN_HAND||!item.isEmpty()||player.isInvisible())return;
        var sample=DogsClient.playerSample(local.getId(),partial);if(sample.weight()<=0)return;
        seamlessdogs$handWeight=sample.weight();
        seamlessdogs$handStroke=sample.stroke();
        seamlessdogs$handHeight=DogsClient.handHeightBias(local.getId());
    }

    // Ordinary callbacks avoid legacy Forge's synthetic Args class-loading failure.
    @Inject(method="renderArmWithItem",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/ItemInHandRenderer;renderPlayerArm(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IFFLnet/minecraft/world/entity/HumanoidArm;)V"))
    private void seamlessdogs$pet(AbstractClientPlayer player, float partial,
        float pitch, InteractionHand hand, float swing, ItemStack item, float equip, PoseStack pose, MultiBufferSource collector, int light, CallbackInfo ci) {
        float weight=seamlessdogs$handWeight;if(weight<=0)return;
        float sign=player.getMainArm()==HumanoidArm.RIGHT?1:-1;
        pose.translate(-sign*.42F*weight,(.40F+seamlessdogs$handHeight*.35F+.025F*seamlessdogs$handStroke)*weight,-.18F*weight);
        pose.mulPose(Axis.XP.rotationDegrees(-12*weight));
        pose.mulPose(Axis.YP.rotationDegrees(sign*6*weight));
        pose.mulPose(Axis.ZP.rotationDegrees(sign*8*weight));
    }

    @ModifyArg(method="renderArmWithItem",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/ItemInHandRenderer;renderPlayerArm(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IFFLnet/minecraft/world/entity/HumanoidArm;)V"),index=4)
    private float seamlessdogs$swing(float swing) {
        return PetAnimation.vanillaSwing(swing,seamlessdogs$handWeight);
    }
}
