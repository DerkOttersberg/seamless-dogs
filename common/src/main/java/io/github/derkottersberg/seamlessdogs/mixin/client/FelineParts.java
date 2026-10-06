package io.github.derkottersberg.seamlessdogs.mixin.client;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(OcelotModel.class)
public interface FelineParts {
    @Accessor("head") ModelPart seamlessdogs$head();
    @Accessor("body") ModelPart seamlessdogs$body();
    @Accessor("tail1") ModelPart seamlessdogs$tail1();
    @Accessor("tail2") ModelPart seamlessdogs$tail2();
    @Accessor("leftFrontLeg") ModelPart seamlessdogs$leftFront();
    @Accessor("rightFrontLeg") ModelPart seamlessdogs$rightFront();
    @Accessor("leftHindLeg") ModelPart seamlessdogs$leftHind();
    @Accessor("rightHindLeg") ModelPart seamlessdogs$rightHind();
}
