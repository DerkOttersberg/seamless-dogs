package qa.dogs.mixin;
import qa.dogs.PetFeatureProbe;
import io.github.derkottersberg.seamlessdogs.client.ActionRenderData;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.world.entity.animal.feline.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CatRenderer.class)
public abstract class CatRenderProbe {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/animal/feline/Cat;Lnet/minecraft/client/renderer/entity/state/CatRenderState;F)V",at=@At("TAIL"))
    private void qa$cat(Cat cat,CatRenderState state,float partial,CallbackInfo ci) {
        var parts=((ActionRenderData)state).seamlessdogs$actionPose().parts();
        Integer expected=PetFeatureProbe.expectedGrooms.get(cat.getUUID());
        if(expected!=null&&parts.containsKey("head_alignment")&&parts.get("head_alignment")[0]>.1F) {
            boolean left=parts.containsKey("left_front_leg"),right=parts.containsKey("right_front_leg");
            int rendered=right&&!left?0:left&&!right?1:!left&&!right?2:-1;
            if(rendered!=expected)throw new IllegalStateException("Server grooming variant differs from rendered rig: "+expected+" / "+rendered);
            PetFeatureProbe.renderedGroomVariants|=1<<rendered;
        }
        if(!((ActionRenderData)state).seamlessdogs$actionPose().parts().isEmpty())PetFeatureProbe.catFrames++;
        if(state.texture.getNamespace().equals("seamlessdogs"))PetFeatureProbe.catEyes++;
    }
}
