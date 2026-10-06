package qa.dogs.fabric;
import qa.dogs.ServerProbe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
public final class DogsQaFabric implements ModInitializer {
    public void onInitialize() {
        var probe = new ServerProbe();
        ServerTickEvents.END_SERVER_TICK.register(probe::tick);
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> ServerProbe.registerCommands(dispatcher));
    }
}
