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
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if(event.phase!=TickEvent.Phase.END)return;
            if(event.getServer().getCommands().getDispatcher().getRoot().getChild("dogsqa")==null)
                throw new IllegalStateException("QA commands missing after normal Forge startup");
            probe.tick(event.getServer());
        });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> ServerProbe.registerCommands(event.getDispatcher()));
        if (FMLEnvironment.dist.isClient()) DogsQaForgeClient.initialize();
    }
}
