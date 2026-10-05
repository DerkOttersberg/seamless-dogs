package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(WolfModel.class)
public abstract class WolfModelMixin {
    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart tail;
    @Unique private float seamlessdogs$renderPartial;
    // WolfRenderer uses the setupAnim age argument for the tail angle.
    // Capture the real frame delta from prepareMobModel instead.
    @Inject(method="prepareMobModel(Lnet/minecraft/world/entity/animal/Wolf;FFF)V",at=@At("HEAD"))
    private void seamlessdogs$frame(Wolf dog,float limb,float amount,float partial,CallbackInfo ci) {
        seamlessdogs$renderPartial=partial;
    }
    @Inject(method="setupAnim(Lnet/minecraft/world/entity/animal/Wolf;FFFFF)V",at=@At("TAIL"))
    private void seamlessdogs$pose(Wolf dog,float limb,float amount,float age,float yaw,float pitch,CallbackInfo ci) {
        var s=ClientOptions.animation ? DogsClient.dogSample(dog.getUUID(),seamlessdogs$renderPartial) : PetAnimation.sample(-1);
        head.xRot+=(-0.15F+0.04F*s.stroke())*s.weight();
        head.zRot+=0.13F*s.weight();
        tail.yRot+=(float)Math.sin((dog.tickCount+seamlessdogs$renderPartial)*0.8F)*0.6F*s.weight();
    }
}
