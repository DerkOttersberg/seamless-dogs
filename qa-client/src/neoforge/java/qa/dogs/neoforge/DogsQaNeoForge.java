package qa.dogs.neoforge;
import qa.dogs.ServerProbe;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
@Mod("seamlessdogsqa")
public final class DogsQaNeoForge {
    public DogsQaNeoForge() {
        var probe = new ServerProbe();
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> probe.tick(event.getServer()));
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> ServerProbe.registerCommands(event.getDispatcher()));
        if (FMLEnvironment.dist.isClient()) DogsQaNeoForgeClient.initialize();
    }
}
