package io.github.derkottersberg.seamlessdogs.mixin.client;
import net.minecraft.client.model.geom.builders.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(PartDefinition.class)
public interface PartDefinitionAccess {
    @Accessor("cubes") java.util.List<CubeDefinition> seamlessdogs$cubes();
    @org.spongepowered.asm.mixin.Mutable @Accessor("cubes") void seamlessdogs$cubes(java.util.List<CubeDefinition> cubes);
}
