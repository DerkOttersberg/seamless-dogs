package io.github.derkottersberg.seamlessdogs.neoforge;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.internal.PlatformServices;
import io.github.derkottersberg.seamlessdogs.network.*;
import java.util.LinkedHashSet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@Mod("seamlessdogs")
public final class DogsNeoForge {
    public DogsNeoForge(IEventBus bus, ModContainer container) {
        bus.addListener(DogsNeoForge::registerPayloads);
        SeamlessDogs.initialize(new Services());
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> SeamlessDogs.tick(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> SeamlessDogs.clear());
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> cancel(event.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> cancel(event.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> cancel(event.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.Clone event) -> { cancel(event.getOriginal()); cancel(event.getEntity()); });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.StartTracking event) -> {
            if (event.getEntity() instanceof ServerPlayer observer) SeamlessDogs.syncTo(observer, event.getTarget());
        });
        if (FMLEnvironment.getDist().isClient()) DogsNeoForgeClient.initialize(bus, container);
    }
    private static void cancel(Entity entity) { if (entity instanceof ServerPlayer player) SeamlessDogs.disconnect(player); }
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().executesOn(HandlerThread.MAIN)
            .playToServer(PetRequest.TYPE, PetRequest.CODEC, (packet, context) -> {
                if (context.player() instanceof ServerPlayer player) SeamlessDogs.request(player, packet);
            }).playToClient(PetState.TYPE, PetState.CODEC, (packet, context) -> DogsNeoForgeClient.receive(packet));
    }
    private static final class Services implements PlatformServices {
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
            if (player.connection != null && NetworkRegistry.hasChannel(player.connection, packet.type().id()))
                PacketDistributor.sendToPlayer(player, packet);
        }
        public void sendToTrackingAndSelf(ServerPlayer player, Entity dog, CustomPacketPayload packet) {
            var recipients = new LinkedHashSet<ServerPlayer>();
            if (player.level() instanceof ServerLevel level) recipients.addAll(level.getChunkSource().chunkMap.getPlayersWatching(player));
            if (dog.level() instanceof ServerLevel level) recipients.addAll(level.getChunkSource().chunkMap.getPlayersWatching(dog));
            recipients.add(player);
            recipients.forEach(recipient -> sendToPlayer(recipient, packet));
        }
    }
}
