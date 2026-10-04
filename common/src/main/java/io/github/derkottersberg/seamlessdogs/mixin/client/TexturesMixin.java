package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.EyeTextures;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(TextureManager.class)
public abstract class TexturesMixin {
    @Inject(method = "reload", at = @At("HEAD"))
    private void seamlessdogs$reload(CallbackInfoReturnable<?> ci) { EyeTextures.clear(); }
}
