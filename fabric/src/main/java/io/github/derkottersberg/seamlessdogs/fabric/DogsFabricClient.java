package io.github.derkottersberg.seamlessdogs.fabric;
import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.client.*;
import io.github.derkottersberg.seamlessdogs.internal.ClientPlatformServices;
import io.github.derkottersberg.seamlessdogs.network.*;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public final class DogsFabricClient implements ClientModInitializer {
    public static final KeyMapping PET = DogsKeys.PET;
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(PET);
        KeyBindingHelper.registerKeyBinding(DogsKeys.SETTINGS);
        DogsClient.initialize(new Services());
        ClientPlayNetworking.registerGlobalReceiver(PetState.TYPE, (state, context) -> context.client().execute(() -> DogsClient.receive(state)));
        ClientPlayNetworking.registerGlobalReceiver(PetUpdate.TYPE, (state,context) -> context.client().execute(() -> DogsClient.receive(state)));
        ClientTickEvents.END_CLIENT_TICK.register(DogsClient::tick);
    }
    private static final class Services implements ClientPlatformServices {
        public Path configDirectory() { return FabricLoader.getInstance().getConfigDir(); }
        public KeyMapping petKey() { return PET; }
        public boolean serverSupportsV2() { return ClientPlayNetworking.canSend(PetControl.TYPE); }
        public boolean serverSupportsPetting() { return ClientPlayNetworking.canSend(PetRequest.TYPE); }
        public void sendToServer(CustomPacketPayload packet) { ClientPlayNetworking.send(packet); }
    }
}
