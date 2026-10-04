package io.github.derkottersberg.seamlessdogs.fabric;
import io.github.derkottersberg.seamlessdogs.client.*;
import io.github.derkottersberg.seamlessdogs.internal.*;
import io.github.derkottersberg.seamlessdogs.network.*;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
public final class DogsFabricClient implements ClientModInitializer {
    public static final KeyMapping PET=DogsKeys.PET;
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(PET); DogsClient.initialize(new Services());
        ClientPlayNetworking.registerGlobalReceiver(PetState.TYPE.id(),(client,handler,buffer,response)-> {
            if(buffer.readableBytes()!=33) return;
            var state=PetState.CODEC.decode(buffer); client.execute(()->DogsClient.receive(state));
        });
        ClientTickEvents.END_CLIENT_TICK.register(DogsClient::tick);
    }
    private static final class Services implements ClientPlatformServices {
        public Path configDirectory(){return FabricLoader.getInstance().getConfigDir();}
        public KeyMapping petKey(){return PET;}
        public boolean serverSupportsPetting(){return ClientPlayNetworking.canSend(PetRequest.TYPE.id());}
        public void sendToServer(DogsPayload packet){var buffer=PacketByteBufs.create(); PetRequest.CODEC.encode(buffer,(PetRequest)packet); ClientPlayNetworking.send(packet.type().id(),buffer);}
    }
}
