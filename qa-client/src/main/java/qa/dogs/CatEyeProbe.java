package qa.dogs;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.derkottersberg.seamlessdogs.client.EyeTextures;
import io.github.derkottersberg.seamlessdogs.client.CatEyelids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import java.nio.file.Files;
/** Checks the actual generated textures for all vanilla coats on the live renderer. */
public final class CatEyeProbe {
    public static void verify(Minecraft c) {
        try {
            var out=c.gameDirectory.toPath().resolve("eye-evidence");Files.createDirectories(out);
            int checked=0;
            for(String coat:new String[]{"all_black","black","british_shorthair","calico","jellie","persian","ragdoll","red","siamese","tabby","white"})
                for(boolean baby:new boolean[]{false}) {
                    var original=ResourceLocation.withDefaultNamespace("textures/entity/cat/cat_"+coat+(baby?"_baby":"")+".png");
                    if(!EyeTextures.catRelaxed(original,baby).equals(original))throw new IllegalStateException("Open eyes must retain the pack texture");
                    try(var input=c.getResourceManager().open(original);var source=NativeImage.read(input)) {
                        for(boolean smile:new boolean[]{false,true}) {
                            var generated=EyeTextures.catExpression(original,smile,baby);
                            if(!(c.getTextureManager().getTexture(generated) instanceof DynamicTexture texture))throw new IllegalStateException("Missing cat lid texture");
                            var pixels=texture.getPixels();int row=baby?5:6,left=0,right=0;
                            for(int x=4;x<=10;x++)for(int y=row;y<=7;y++)if(CatEyelids.crease(baby,smile,x,y)) {
                                int pixel=pixels.getPixelRGBA(x,y),fur=source.getPixelRGBA(x,row-1);
                                if(pixel==fur||(pixel>>>24)!=255)throw new IllegalStateException("Invisible lid "+original);
                                if(x<7)left++;else right++;
                            }
                            if(left==0||right==0)throw new IllegalStateException("Missing cat eye");
                            pixels.writeToFile(out.resolve(coat+(baby?"-baby":"-adult")+(smile?"-smile":"-blink")+".png"));checked++;
                        }
                        source.writeToFile(out.resolve(coat+(baby?"-baby":"-adult")+"-open.png"));
                    }
                }
            Files.writeString(out.resolve("passed.txt"),"PASS original open textures and "+checked+" opaque, visible closed-eye textures across all 11 vanilla coats shared by adults and scaled kittens.\n");
            ClientProbe.log("CAT_EYE_TEXTURE_PASS "+checked);
        }catch(Exception failure){throw new IllegalStateException("Cat eye regression",failure);}
    }
}
