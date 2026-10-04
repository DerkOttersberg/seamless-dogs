package io.github.derkottersberg.seamlessdogs.forge;

import io.github.derkottersberg.seamlessdogs.client.*;
import io.github.derkottersberg.seamlessdogs.internal.ClientPlatformServices;
import io.github.derkottersberg.seamlessdogs.network.PetState;
import java.nio.file.Path;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import io.github.derkottersberg.seamlessdogs.internal.DogsPayload;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

final class DogsForgeClient {
    static void initialize(FMLJavaModLoadingContext context) {
        context.getModEventBus().addListener((RegisterKeyMappingsEvent event) -> event.register(DogsKeys.PET));
        DogsClient.initialize(new Services());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> { if(event.phase == TickEvent.Phase.END) DogsClient.tick(Minecraft.getInstance()); });
        net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> new DogsSettingsScreen(parent)));
    }
    static void receive(PetState packet) { DogsClient.receive(packet); }
    private static final class Services implements ClientPlatformServices {
        public Path configDirectory() { return FMLPaths.CONFIGDIR.get(); }
        public KeyMapping petKey() { return DogsKeys.PET; }
        public boolean serverSupportsPetting() {
            var connection = Minecraft.getInstance().getConnection();
            return connection != null && DogsForge.supports(connection.getConnection());
        }
        public void sendToServer(DogsPayload packet) { DogsForge.send(packet); }
    }
}
