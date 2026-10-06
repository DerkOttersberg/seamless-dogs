package qa.dogs.mixin;
import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.gameplay.PetWorldState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
/** Test-only access for accelerating time in a disposable world. Never packaged in the product. */
@Mixin(SeamlessDogs.class)
public interface WorldStateAccess {
    @Accessor("platform") static io.github.derkottersberg.seamlessdogs.internal.PlatformServices qa$platform() {throw new AssertionError();}
    @Accessor("world") static PetWorldState qa$world() { throw new AssertionError(); }
    @Invoker("startVariant") static void qa$startVariant(net.minecraft.server.level.ServerPlayer owner,net.minecraft.world.entity.TamableAnimal pet,io.github.derkottersberg.seamlessdogs.gameplay.PetAction action,net.minecraft.core.BlockPos site,int variant) {throw new AssertionError();}
    @Invoker("start") static void qa$start(net.minecraft.server.level.ServerPlayer owner,net.minecraft.world.entity.TamableAnimal pet,io.github.derkottersberg.seamlessdogs.gameplay.PetAction action,net.minecraft.core.BlockPos site) {throw new AssertionError();}
}
