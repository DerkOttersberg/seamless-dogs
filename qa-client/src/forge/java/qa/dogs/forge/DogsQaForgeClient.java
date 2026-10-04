package qa.dogs.forge;
import qa.dogs.ClientProbe;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
final class DogsQaForgeClient {
    static void initialize() {
        var probe = new ClientProbe();
        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> probe.tick(Minecraft.getInstance()));
    }
}
