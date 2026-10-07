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
            if(event.phase!=TickEvent.Phase.END) return;
            var server=event.getServer();
            if(server.getCommands().getDispatcher().getRoot().getChild("dogsqa")==null)
                throw new IllegalStateException("QA command registration missing; do not open the world before Forge finishes loading");
            probe.tick(server);
        });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL, false, RegisterCommandsEvent.class, event -> {
            System.out.println("DOGS_QA_REGISTER_COMMAND_EVENT environment="+event.getCommandSelection());
            ServerProbe.registerCommands(event.getDispatcher());
        });
        if (FMLEnvironment.dist.isClient()) DogsQaForgeClient.initialize();
    }
}
