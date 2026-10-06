package io.github.derkottersberg.seamlessdogs.mixin.client;
import net.minecraft.client.model.geom.builders.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(LayerDefinition.class)
public interface LayerDefinitionAccess {
    @Accessor("mesh") MeshDefinition seamlessdogs$mesh();
}
