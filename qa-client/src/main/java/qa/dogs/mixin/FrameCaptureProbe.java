package qa.dogs.mixin;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Test-only screenshot readback after all hand and HUD draws finish. */
@Mixin(Minecraft.class)
public abstract class FrameCaptureProbe {
    @Inject(method="runTick",at=@At("TAIL"))
    private void qa$completedFrame(CallbackInfo ci) { qa.dogs.HandReturnProbe.captureAfterFrame(Minecraft.getInstance()); qa.dogs.ObserverProbe.captureAfterFrame(Minecraft.getInstance()); }
}
