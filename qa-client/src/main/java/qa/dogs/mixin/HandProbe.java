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
    @Inject(method="renderPlayerArm",at=@At("TAIL"))
    private void qa$hand(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player != null && DogsClient.playerSample(player.getId(), 0).weight() > 0.2F) ClientProbe.hands++;
    }
}
