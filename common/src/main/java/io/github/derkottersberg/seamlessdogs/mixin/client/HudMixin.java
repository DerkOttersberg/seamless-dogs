package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.DogsClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Gui.class)
public abstract class HudMixin {
    @Inject(method="render",at=@At("TAIL"))
    private void seamlessdogs$prompt(GuiGraphics graphics, float partial, CallbackInfo ci) { DogsClient.prompt(graphics); }
}
