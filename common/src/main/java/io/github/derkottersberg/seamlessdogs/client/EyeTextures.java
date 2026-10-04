package io.github.derkottersberg.seamlessdogs.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/** Derive eyelids from the current pack's fur pixels; no Mojang texture is shipped. */
public final class EyeTextures {
    private static final Map<String, Identifier> cache = new HashMap<>();
    public static Identifier expression(Identifier original, boolean happy) {
        String key = original + ":" + happy;
        return cache.computeIfAbsent(key, ignored -> create(original, happy));
    }
    private static Identifier create(Identifier original, boolean happy) {
        var c = Minecraft.getInstance();
        try (var input = c.getResourceManager().open(original); var source = NativeImage.read(input)) {
            // Only the vanilla adult UV layout. Baby UVs and arbitrary layouts fall back.
            if (source.getWidth() % 64 != 0 || source.getHeight() * 2 != source.getWidth()) return original;
            int scale = source.getWidth() / 64;
            var modified = new NativeImage(source.getWidth(), source.getHeight(), false);
            modified.copyFrom(source);
            for (int x : new int[] {4, 5, 8, 9}) {
                int fur = source.getPixel(x * scale, 4 * scale);
                for (int y : new int[] {5, 6}) for (int dx = 0; dx < scale; dx++) for (int dy = 0; dy < scale; dy++) {
                    int color = fur;
                    // A little closed-eye smile: dark outside corner and raised inner tip.
                    if (happy && ((y == 6 && (x == 4 || x == 9)) || (y == 5 && (x == 5 || x == 8)))) color = 0xFF302B29;
                    modified.setPixel(x * scale + dx, y * scale + dy, color);
                }
            }
            Identifier id = SeamlessDogs.id("eyes/" + original.getNamespace() + "/" + original.getPath() + (happy ? "_happy" : "_blink"));
            c.getTextureManager().register(id, new DynamicTexture(() -> "Seamless Dogs eyelids", modified));
            return id;
        } catch (Exception e) { return original; }
    }
    /** Called before the texture manager rebuilds textures on a pack reload. */
    public static void clear() {
        var manager = Minecraft.getInstance().getTextureManager();
        for (Identifier id : cache.values()) if (id.getNamespace().equals("seamlessdogs")) manager.release(id);
        cache.clear();
    }
}
