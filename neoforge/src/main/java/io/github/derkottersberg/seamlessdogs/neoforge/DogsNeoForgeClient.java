package io.github.derkottersberg.seamlessdogs.neoforge;

import io.github.derkottersberg.seamlessdogs.client.*;
import io.github.derkottersberg.seamlessdogs.internal.ClientPlatformServices;
import io.github.derkottersberg.seamlessdogs.network.*;
import java.nio.file.Path;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

final class DogsNeoForgeClient {
    static void initialize(IEventBus bus, ModContainer container) {
        bus.addListener((RegisterKeyMappingsEvent event) -> event.register(DogsKeys.PET));
        DogsClient.initialize(new Services());
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> DogsClient.tick(Minecraft.getInstance()));
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new DogsSettingsScreen(parent));
    }
    static void receive(PetState packet) { DogsClient.receive(packet); }
    private static final class Services implements ClientPlatformServices {
        public Path configDirectory() { return FMLPaths.CONFIGDIR.get(); }
        public KeyMapping petKey() { return DogsKeys.PET; }
        public boolean serverSupportsPetting() {
            var connection = Minecraft.getInstance().getConnection();
            return connection != null && NetworkRegistry.hasChannel(connection, PetRequest.TYPE.id());
        }
        public void sendToServer(CustomPacketPayload packet) { ClientPacketDistributor.sendToServer(packet); }
    }
}
