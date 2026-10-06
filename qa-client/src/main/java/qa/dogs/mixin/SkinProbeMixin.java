package qa.dogs.mixin;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(AbstractClientPlayer.class)
public abstract class SkinProbeMixin {
    @Inject(method="getSkin",at=@At("RETURN"),cancellable=true)
    private void qa$skin(CallbackInfoReturnable<net.minecraft.client.resources.PlayerSkin> ci){ci.setReturnValue(qa.dogs.SkinProbe.skin());}
}
