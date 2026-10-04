package qa.dogs.fabric;
import qa.dogs.ClientProbe;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
public final class DogsQaFabricClient implements ClientModInitializer {
    public void onInitializeClient() { ClientTickEvents.END_CLIENT_TICK.register(new ClientProbe()::tick); }
}
