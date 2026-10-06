package io.github.derkottersberg.seamlessdogs.forge;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.internal.PlatformServices;
import io.github.derkottersberg.seamlessdogs.network.PetRequest;
import io.github.derkottersberg.seamlessdogs.network.*;
import java.util.LinkedHashSet;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;

@Mod("seamlessdogs")
public final class DogsForge {
    private static final Channel<CustomPacketPayload> REQUEST = ChannelBuilder.named(SeamlessDogs.id("request"))
        .networkProtocolVersion(1).optional().payloadChannel().play().serverbound()
        .addMain(PetRequest.TYPE, PetRequest.CODEC, (packet, context) -> {
            if (context.getSender() != null) SeamlessDogs.request(context.getSender(), packet);
        }).build();
    private static final Channel<CustomPacketPayload> STATE = ChannelBuilder.named(SeamlessDogs.id("state"))
        .networkProtocolVersion(1).optional().payloadChannel().play().clientbound()
        .addMain(PetState.TYPE, PetState.CODEC, (packet, context) -> DogsForgeClient.receive(packet)).build();

    private static final Channel<CustomPacketPayload> CONTROL = ChannelBuilder.named(SeamlessDogs.id("control_v2"))
        .networkProtocolVersion(2).optional().payloadChannel().play().serverbound()
        .addMain(PetControl.TYPE,PetControl.CODEC,(packet,context)-> { if(context.getSender()!=null)SeamlessDogs.control(context.getSender(),packet); }).build();
    private static final Channel<CustomPacketPayload> UPDATE = ChannelBuilder.named(SeamlessDogs.id("update_v2"))
        .networkProtocolVersion(2).optional().payloadChannel().play().clientbound()
        .addMain(PetUpdate.TYPE,PetUpdate.CODEC,(packet,context)->DogsForgeClient.receive(packet)).build();

    public DogsForge(FMLJavaModLoadingContext context) {
        SeamlessDogs.initialize(new Services());
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> SeamlessDogs.tick(event.server()));
        ServerStoppedEvent.BUS.addListener(event -> SeamlessDogs.clear());
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(event -> cancel(event.getEntity()));
        PlayerEvent.PlayerChangedDimensionEvent.BUS.addListener(event -> cancel(event.getEntity()));
        PlayerEvent.PlayerRespawnEvent.BUS.addListener(event -> cancel(event.getEntity()));
        PlayerEvent.Clone.BUS.addListener(event -> { cancel(event.getOriginal()); cancel(event.getEntity()); });
        PlayerEvent.StartTracking.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer observer) SeamlessDogs.syncTo(observer, event.getTarget());
        });
        if (FMLEnvironment.dist.isClient()) DogsForgeClient.initialize(context);
    }
    private static void cancel(Entity entity) { if (entity instanceof ServerPlayer player) SeamlessDogs.disconnect(player); }
    static boolean supports(Connection connection) { return REQUEST.isRemotePresent(connection); }
    static boolean supportsV2(Connection connection) { return CONTROL.isRemotePresent(connection); }
    static void send(CustomPacketPayload packet) { (packet instanceof PetControl ? CONTROL : REQUEST).send(packet, PacketDistributor.SERVER.noArg()); }

    private static final class Services implements PlatformServices {
        public boolean supportsV2(ServerPlayer player) { return player.connection!=null && UPDATE.isRemotePresent(player.connection.getConnection()); }
        public boolean mayDig(ServerPlayer owner, net.minecraft.world.entity.TamableAnimal pet, net.minecraft.core.BlockPos pos, boolean commit) { return DogsForgeProtection.allowed(owner,pet,pos,commit); }
        public String protectionStatus() { return DogsForgeProtection.status(); }
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
            if(packet instanceof PetUpdate update && !SeamlessDogs.mayReceive(player,update))return;
            if(packet instanceof PetUpdate) { if(supportsV2(player)) UPDATE.send(packet,PacketDistributor.PLAYER.with(player)); return; }
            if(supportsV2(player)) return;
            if (player.connection != null && STATE.isRemotePresent(player.connection.getConnection()))
                STATE.send(packet, PacketDistributor.PLAYER.with(player));
        }
        public void sendToTrackingAndSelf(ServerPlayer player, Entity dog, CustomPacketPayload packet) {
            var recipients = new LinkedHashSet<ServerPlayer>();
            addTracking(player, recipients);
            addTracking(dog, recipients);
            recipients.add(player);
            recipients.forEach(recipient -> sendToPlayer(recipient, packet));
        }
        private static void addTracking(Entity entity, LinkedHashSet<ServerPlayer> recipients) {
            if (!(entity.level() instanceof ServerLevel level)) return;
            var chunks = level.getChunkSource().chunkMap;
            for (ServerPlayer observer : chunks.getPlayers(entity.chunkPosition(), false)) {
                chunks.forEachEntityTrackedBy(observer, tracked -> { if (tracked == entity) recipients.add(observer); });
            }
        }
    }
}
