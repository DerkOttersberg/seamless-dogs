package qa.dogs.neoforge;
import qa.dogs.ClientProbe;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
final class DogsQaNeoForgeClient {
    static void initialize() {
        var probe = new ClientProbe();
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> probe.tick(Minecraft.getInstance()));
    }
}
