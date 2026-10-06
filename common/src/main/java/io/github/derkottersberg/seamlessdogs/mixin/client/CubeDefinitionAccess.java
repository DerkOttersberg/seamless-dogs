package io.github.derkottersberg.seamlessdogs.mixin.client;
import net.minecraft.client.model.geom.builders.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(CubeDefinition.class)
public interface CubeDefinitionAccess {
    @Accessor("origin") org.joml.Vector3f seamlessdogs$origin();
    @Accessor("dimensions") org.joml.Vector3f seamlessdogs$dimensions();
}
