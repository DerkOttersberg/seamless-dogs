package io.github.derkottersberg.seamlessdogs.mixin.client;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Move the exact native ear cubes into bones, retaining original UVs/deformation. */
@Mixin(WolfModel.class)
public abstract class WolfEarRigMixin {
    @Inject(method="createBodyLayer()Lnet/minecraft/client/model/geom/builders/LayerDefinition;",at=@At("RETURN"))
    private static void seamlessdogs$ears(CallbackInfoReturnable<LayerDefinition> ci){
        var mesh=((LayerDefinitionAccess)(Object)ci.getReturnValue()).seamlessdogs$mesh();
        var head=mesh.getRoot().getChild("head").getChild("real_head");
        var access=(PartDefinitionAccess)(Object)head;
        var remaining=new java.util.ArrayList<>(access.seamlessdogs$cubes());
        for(var cube:access.seamlessdogs$cubes()){
            var data=(CubeDefinitionAccess)(Object)cube;var origin=data.seamlessdogs$origin();var size=data.seamlessdogs$dimensions();
            if((origin.x==-2||origin.x==2)&&origin.y==-5&&origin.z==0&&size.x==2&&size.y==2&&size.z==1){
                boolean left=origin.x==2;float x=left?3:-1;
                var ear=head.addOrReplaceChild("seamlessdogs_"+(left?"left":"right")+"_ear",CubeListBuilder.create(),PartPose.offset(x,-3,.5F));
                origin.sub(x,-3,.5F);
                ((PartDefinitionAccess)(Object)ear).seamlessdogs$cubes(java.util.List.of(cube));remaining.remove(cube);
            }
        }
        access.seamlessdogs$cubes(remaining);
    }
}
