package io.github.derkottersberg.seamlessdogs.mixin.client;

import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Articulate vanilla's two ear cubes, retaining their exact rest geometry, UVs and deformation. */
@Mixin(AdultWolfModel.class)
public abstract class WolfEarRigMixin {
    @Redirect(method="createBodyLayer",at=@At(value="INVOKE",target="Lnet/minecraft/client/model/geom/builders/CubeListBuilder;addBox(FFFFFFLnet/minecraft/client/model/geom/builders/CubeDeformation;)Lnet/minecraft/client/model/geom/builders/CubeListBuilder;"))
    private static CubeListBuilder seamlessdogs$earCube(CubeListBuilder builder,float x,float y,float z,float w,float h,float d,CubeDeformation deformation) {
        if((x==-2||x==2)&&y==-5&&z==0&&w==2&&h==2&&d==1)return builder;
        return builder.addBox(x,y,z,w,h,d,deformation);
    }
    @Inject(method="createBodyLayer",at=@At("RETURN"))
    private static void seamlessdogs$earBones(CubeDeformation deformation,CallbackInfoReturnable<MeshDefinition> ci) {
        var head=ci.getReturnValue().getRoot().getChild("head").getChild("real_head");
        for(boolean left:new boolean[]{false,true})head.addOrReplaceChild("seamlessdogs_"+(left?"left":"right")+"_ear",
            CubeListBuilder.create().texOffs(16,14).addBox(-1,-2,-.5F,2,2,1,deformation),PartPose.offset(left?3:-1,-3,.5F));
    }
}
