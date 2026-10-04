package io.github.derkottersberg.seamlessdogs.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/** Derive eyelids from the current pack's fur pixels; no Mojang texture is shipped. */
public final class EyeTextures {
    private static final Map<String, ResourceLocation> cache = new HashMap<>();
    public static ResourceLocation expression(ResourceLocation original, boolean happy) {
        return expression(original, happy, false);
    }
    public static ResourceLocation expression(ResourceLocation original, boolean happy, boolean baby) {
        String key = original + ":" + happy + ":" + baby;
        return cache.computeIfAbsent(key, ignored -> create(original, happy, baby));
    }
    private static ResourceLocation create(ResourceLocation original, boolean happy, boolean baby) {
        var c = Minecraft.getInstance();
        try (var input = c.getResourceManager().open(original); var source = NativeImage.read(input)) {
            // Vanilla adult 64x32 and puppy 32x32 layouts, including integer-scaled packs.
            int baseWidth = baby ? 32 : 64;
            if (source.getWidth() % baseWidth != 0 || source.getHeight() * baseWidth != source.getWidth() * 32) return original;
            int scale = source.getWidth() / baseWidth;
            var modified = new NativeImage(source.getWidth(), source.getHeight(), false);
            modified.copyFrom(source);
            int xOffset = baby ? 1 : 0, yOffset = baby ? 13 : 0;
            for (int x : new int[] {4, 5, 8, 9}) {
                for (int y : new int[] {5, 6}) for (int dx = 0; dx < scale; dx++) for (int dy = 0; dy < scale; dy++) {
                    int color = source.getPixelRGBA((x + xOffset) * scale + dx, (4 + yOffset) * scale + dy);
                    // A little closed-eye smile: dark outside corner and raised inner tip.
                    if (happy && ((y == 6 && (x == 4 || x == 9)) || (y == 5 && (x == 5 || x == 8)))) color = 0xFF292B30;
                    modified.setPixelRGBA((x + xOffset) * scale + dx, (y + yOffset) * scale + dy, color);
                }
            }
            ResourceLocation id = SeamlessDogs.id("eyes/" + original.getNamespace() + "/" + original.getPath() + (happy ? "_happy" : "_blink"));
            c.getTextureManager().register(id, new DynamicTexture(modified));
            return id;
        } catch (Exception e) { return original; }
    }
    /** Called before the texture manager rebuilds textures on a pack reload. */
    public static void clear() {
        var manager = Minecraft.getInstance().getTextureManager();
        for (ResourceLocation id : cache.values()) if (id.getNamespace().equals("seamlessdogs")) manager.release(id);
        cache.clear();
    }
}
