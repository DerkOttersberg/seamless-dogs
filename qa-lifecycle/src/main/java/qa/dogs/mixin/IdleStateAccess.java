package qa.dogs.mixin;
import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.gameplay.PetAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(SeamlessDogs.class)
public interface IdleStateAccess {
    @Invoker("startVariant") static void qa$start(ServerPlayer owner,TamableAnimal pet,PetAction action,BlockPos site,int variant){throw new AssertionError();}
    @Accessor("sessions") static java.util.Map<java.util.UUID,?> qa$sessions(){throw new AssertionError();}
}
