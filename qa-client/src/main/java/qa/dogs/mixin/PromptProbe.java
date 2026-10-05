package qa.dogs.mixin;
import qa.dogs.ClientProbe;
import io.github.derkottersberg.seamlessdogs.client.DogsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=DogsClient.class,remap=false)
public abstract class PromptProbe {
    @Inject(method="prompt",at=@At("TAIL"))
    private static void qa$prompt(CallbackInfo ci) { if (DogsClient.target() != null) ClientProbe.prompts++; }
}
