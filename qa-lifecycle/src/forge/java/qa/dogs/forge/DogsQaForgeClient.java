package qa.dogs.forge;
import qa.dogs.ClientProbe;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
final class DogsQaForgeClient {
    static void initialize() {
        var probe = new ClientProbe();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if(event.phase==TickEvent.Phase.END && DogsQaStartup.tick(Minecraft.getInstance()))probe.tick(Minecraft.getInstance());
        });
    }
}
