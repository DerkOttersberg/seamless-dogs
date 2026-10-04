package io.github.derkottersberg.seamlessdogs.fabric;
import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.internal.*;
import io.github.derkottersberg.seamlessdogs.network.*;
import java.util.LinkedHashSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
public final class DogsFabric implements ModInitializer {
    public void onInitialize() {
        SeamlessDogs.initialize(new Services());
        ServerPlayNetworking.registerGlobalReceiver(PetRequest.TYPE.id(),(server,player,handler,buffer,response) -> {
            if (buffer.readableBytes()!=4) return;
            var request=PetRequest.CODEC.decode(buffer);
            server.execute(() -> SeamlessDogs.request(player,request));
        });
        ServerTickEvents.END_SERVER_TICK.register(SeamlessDogs::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->SeamlessDogs.disconnect(handler.player));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity,level)-> { if(entity instanceof ServerPlayer player) SeamlessDogs.disconnect(player); });
        EntityTrackingEvents.START_TRACKING.register((entity,observer)->SeamlessDogs.syncTo(observer,entity));
        ServerLifecycleEvents.SERVER_STOPPED.register(server->SeamlessDogs.clear());
    }
    private static final class Services implements PlatformServices {
        public void sendToPlayer(ServerPlayer player,DogsPayload payload) {
            if(player.connection==null || !ServerPlayNetworking.canSend(player,payload.type().id())) return;
            var buffer=PacketByteBufs.create(); PetState.CODEC.encode(buffer,(PetState)payload);
            ServerPlayNetworking.send(player,payload.type().id(),buffer);
        }
        public void sendToTrackingAndSelf(ServerPlayer player,Entity dog,DogsPayload payload) {
            var recipients=new LinkedHashSet<>(PlayerLookup.tracking(player));
            recipients.addAll(PlayerLookup.tracking(dog)); recipients.add(player);
            recipients.forEach(recipient->sendToPlayer(recipient,payload));
        }
    }
}
