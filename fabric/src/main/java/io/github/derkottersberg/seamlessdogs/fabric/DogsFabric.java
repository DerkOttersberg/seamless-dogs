package io.github.derkottersberg.seamlessdogs.fabric;
import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.internal.PlatformServices;
import io.github.derkottersberg.seamlessdogs.network.*;
import java.util.LinkedHashSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public final class DogsFabric implements ModInitializer {
    public void onInitialize() {
        SeamlessDogs.initialize(new Services());
        PayloadTypeRegistry.serverboundPlay().register(PetRequest.TYPE, PetRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PetControl.TYPE, PetControl.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PetUpdate.TYPE, PetUpdate.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PetControl.TYPE, (packet, context) -> context.server().execute(() -> SeamlessDogs.control(context.player(),packet)));
        PayloadTypeRegistry.clientboundPlay().register(PetState.TYPE, PetState.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PetRequest.TYPE, (packet, context) -> context.server().execute(() -> SeamlessDogs.request(context.player(), packet)));
        ServerTickEvents.END_SERVER_TICK.register(SeamlessDogs::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SeamlessDogs.disconnect(handler.getPlayer()));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> { if (entity instanceof ServerPlayer player) SeamlessDogs.disconnect(player); });
        EntityTrackingEvents.START_TRACKING.register((entity, observer) -> SeamlessDogs.syncTo(observer, entity));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SeamlessDogs.clear());
    }
    private static final class Services implements PlatformServices {
        public boolean supportsV2(ServerPlayer player) { return ServerPlayNetworking.canSend(player,PetUpdate.TYPE); }
        public boolean mayDig(ServerPlayer owner, net.minecraft.world.entity.TamableAnimal pet, net.minecraft.core.BlockPos pos, boolean commit) {
            if (!DogsFabricProtection.allowed(owner,pet,pos)) return false;
            return !commit || net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(pet.level(),owner,pos,pet.level().getBlockState(pos),null);
        }
        public String protectionStatus() { return DogsFabricProtection.status(); }
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
            if(packet instanceof PetUpdate update && !SeamlessDogs.mayReceive(player,update))return;
            if (packet instanceof PetState && supportsV2(player)) return;
            if (player.connection != null && ServerPlayNetworking.canSend(player, packet.type())) ServerPlayNetworking.send(player, packet);
        }
        public void sendToTrackingAndSelf(ServerPlayer player, Entity dog, CustomPacketPayload packet) {
            var recipients = new LinkedHashSet<>(PlayerLookup.tracking(player));
            recipients.addAll(PlayerLookup.tracking(dog)); recipients.add(player);
            recipients.forEach(recipient -> sendToPlayer(recipient, packet));
        }
    }
}
