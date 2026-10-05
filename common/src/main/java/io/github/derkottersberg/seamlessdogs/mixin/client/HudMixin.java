package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.DogsClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void seamlessdogs$prompt(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) { DogsClient.prompt(graphics); }
}
