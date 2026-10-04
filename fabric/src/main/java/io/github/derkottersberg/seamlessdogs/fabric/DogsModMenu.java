package io.github.derkottersberg.seamlessdogs.fabric;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import io.github.derkottersberg.seamlessdogs.client.DogsSettingsScreen;
public final class DogsModMenu implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() { return DogsSettingsScreen::new; }
}
