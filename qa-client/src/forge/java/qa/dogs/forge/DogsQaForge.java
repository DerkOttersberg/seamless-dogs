package qa.dogs.forge;
import qa.dogs.ServerProbe;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
@Mod("seamlessdogsqa")
public final class DogsQaForge {
    public DogsQaForge() {
        var probe = new ServerProbe();
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> probe.tick(event.server()));
        RegisterCommandsEvent.BUS.addListener(event -> ServerProbe.registerCommands(event.getDispatcher()));
        if (FMLEnvironment.dist.isClient()) DogsQaForgeClient.initialize();
    }
}
