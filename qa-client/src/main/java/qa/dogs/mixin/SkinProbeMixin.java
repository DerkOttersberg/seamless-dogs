package qa.dogs.mixin;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(AbstractClientPlayer.class)
public abstract class SkinProbeMixin {
    @Inject(method="getModelName",at=@At("RETURN"),cancellable=true)
    private void qa$model(CallbackInfoReturnable<String> ci){ci.setReturnValue(qa.dogs.SkinProbe.slim?"slim":"default");}
    @Inject(method="getSkinTextureLocation",at=@At("RETURN"),cancellable=true)
    private void qa$skin(CallbackInfoReturnable<net.minecraft.resources.ResourceLocation> ci){ci.setReturnValue(qa.dogs.SkinProbe.skin());}
}
