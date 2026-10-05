package qa.dogs.fabric;
import qa.dogs.ClientProbe;
import io.github.derkottersberg.seamlessdogs.client.DogsSettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.terraformersmc.modmenu.ModMenu;
public final class DogsQaFabricClient implements ClientModInitializer {
    public void onInitializeClient() {
        ClientProbe.settingsOpener = parent -> {
            if (FabricLoader.getInstance().isModLoaded("modmenu")) {
                var screen = ModMenu.getConfigScreen("seamlessdogs", parent);
                if (!(screen instanceof DogsSettingsScreen)) throw new IllegalStateException("Missing Mod Menu config integration");
                ClientProbe.log("DOGS_MODMENU_CONFIG_PASS"); return screen;
            }
            return new DogsSettingsScreen(parent);
        };
        ClientTickEvents.END_CLIENT_TICK.register(new ClientProbe()::tick);
    }
}
