package qa.dogs.mixin;
import qa.dogs.ClientProbe;
import io.github.derkottersberg.seamlessdogs.client.DogsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandRenderer.class)
public abstract class HandProbe {
    @Inject(method="renderArmWithItem",at=@At("HEAD"))
    private void qa$before(net.minecraft.client.player.AbstractClientPlayer player,float partial,float pitch,net.minecraft.world.InteractionHand hand,float swing,net.minecraft.world.item.ItemStack item,float equip,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource collector,int light,CallbackInfo ci) {
        if(hand==net.minecraft.world.InteractionHand.MAIN_HAND&&item.isEmpty())qa.dogs.HandReturnProbe.begin(pose,partial,swing);
    }
    @Inject(method="renderPlayerArm",at=@At("HEAD"))
    private void qa$matrix(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource collector,int light,float equip,float swing,net.minecraft.world.entity.HumanoidArm arm,CallbackInfo ci) {
        qa.dogs.HandReturnProbe.arm(pose,swing,arm);
    }
    @Inject(method="renderPlayerArm",at=@At("TAIL"))
    private void qa$hand(CallbackInfo ci) {
        var player=Minecraft.getInstance().player;
        if(player!=null&&DogsClient.playerSample(player.getId(),0).weight()>.2F){
            ClientProbe.hands++;
            qa.dogs.SkinProbe.record(player.getSkin().model().name());
        }
    }
}
