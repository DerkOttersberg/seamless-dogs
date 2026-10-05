package qa.dogs.mixin;
import io.github.derkottersberg.seamlessdogs.client.DogsClient;
import io.github.derkottersberg.seamlessdogs.internal.ClientPlatformServices;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=DogsClient.class,remap=false)
public interface ClientServicesProbe {
    @Accessor("platform") static ClientPlatformServices qa$platform() { throw new AssertionError(); }
}
