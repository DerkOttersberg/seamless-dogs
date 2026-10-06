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
    public static Identifier catRelaxed(Identifier original, boolean baby) {
        // Vanilla cats have only one iris row. A half-lid erases it completely.
        return original;
    }
    public static Identifier catExpression(Identifier original, boolean happy, boolean baby) {
        if (!original.getNamespace().equals("minecraft") || !original.getPath().startsWith("textures/entity/cat/")) return original;
        String key = "cat:" + original + ":" + happy + ":" + baby;
        return cache.computeIfAbsent(key, ignored -> createCat(original,happy,baby));
    }
    private static Identifier createCat(Identifier original,boolean happy,boolean baby) {
        var c=Minecraft.getInstance();
        try(var input=c.getResourceManager().open(original);var source=NativeImage.read(input)) {
            int base=baby?32:64;
            if(!CatEyelids.supported(source.getWidth(),source.getHeight(),baby))return original;
            int scale=source.getWidth()/base, row=baby?5:6;
            var modified=new NativeImage(source.getWidth(),source.getHeight(),false);modified.copyFrom(source);
            int[] columns=baby?new int[]{5,7}:new int[]{5,6,8,9};
            for(int x:columns)for(int y=row;y<=row+(baby?0:1);y++)for(int dx=0;dx<scale;dx++)for(int dy=0;dy<scale;dy++) {
                int color=source.getPixel(x*scale+dx,(row-1)*scale+dy);
                if(CatEyelids.crease(baby,happy,x,y))color=CatEyelids.lidColor(color);
                modified.setPixel(x*scale+dx,y*scale+dy,color);
            }
            Identifier id=SeamlessDogs.id("eyes/cat/"+original.getPath()+(happy?"_happy":"_blink")+(baby?"_baby":"_adult"));
            c.getTextureManager().register(id,new DynamicTexture(()->"Seamless cat eyelids",modified));return id;
        }catch(Exception failure){return original;}
    }
    public static Identifier expression(Identifier original, boolean happy) {
        return expression(original, happy, false);
    }
    public static Identifier expression(Identifier original, boolean happy, boolean baby) {
        if(!original.getNamespace().equals("minecraft")||!original.getPath().startsWith("textures/entity/wolf/"))return original;
        String key = original + ":" + happy + ":" + baby;
        return cache.computeIfAbsent(key, ignored -> create(original, happy, baby));
    }
    private static Identifier create(Identifier original, boolean happy, boolean baby) {
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
                    int color = source.getPixel((x + xOffset) * scale + dx, (4 + yOffset) * scale + dy);
                    // A little closed-eye smile: dark outside corner and raised inner tip.
                    if (happy && ((y == 6 && (x == 4 || x == 9)) || (y == 5 && (x == 5 || x == 8)))) color = 0xFF302B29;
                    modified.setPixel((x + xOffset) * scale + dx, (y + yOffset) * scale + dy, color);
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
