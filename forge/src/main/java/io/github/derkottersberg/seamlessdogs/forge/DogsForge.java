package io.github.derkottersberg.seamlessdogs.forge;
import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.internal.*;
import io.github.derkottersberg.seamlessdogs.network.*;
import java.util.Optional;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.network.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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

    public DogsForge(){
        SeamlessDogs.initialize(new Services());
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event)-> {if(event.phase==TickEvent.Phase.END) SeamlessDogs.tick(event.getServer());});
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event)->SeamlessDogs.clear());
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event)->cancel(event.getEntity()));
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event)->cancel(event.getEntity()));
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event)->cancel(event.getEntity()));
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.Clone event)->{cancel(event.getOriginal());cancel(event.getEntity());});
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.StartTracking event)->{if(event.getEntity() instanceof ServerPlayer observer) SeamlessDogs.syncTo(observer,event.getTarget());});
        if(FMLEnvironment.dist.isClient()) DogsForgeClient.initialize(FMLJavaModLoadingContext.get());
    }
    private static void cancel(Entity entity){if(entity instanceof ServerPlayer player) SeamlessDogs.disconnect(player);}
    static boolean supports(net.minecraft.network.Connection connection){return REQUEST.isRemotePresent(connection);}
    static boolean supportsV2(Connection connection) { return CONTROL.isRemotePresent(connection); }
    static void send(CustomPacketPayload packet) { (packet instanceof PetControl ? CONTROL : REQUEST).send(packet, PacketDistributor.SERVER.noArg()); }

    private static final class Services implements PlatformServices {
        public boolean supportsV2(ServerPlayer player) { return player.connection!=null && UPDATE.isRemotePresent(player.connection.getConnection()); }
        public boolean mayDig(ServerPlayer owner, net.minecraft.world.entity.TamableAnimal pet, net.minecraft.core.BlockPos pos, boolean commit) { return DogsForgeProtection.allowed(owner,pet,pos,commit); }
        public String protectionStatus() { return DogsForgeProtection.status(); }
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
            if(packet instanceof PetUpdate update&&!SeamlessDogs.mayReceive(player,update))return;
            if(packet instanceof PetUpdate) { if(supportsV2(player)) UPDATE.send(packet,PacketDistributor.PLAYER.with(player)); return; }
            if(supportsV2(player)) return;
            if (player.connection != null && STATE.isRemotePresent(player.connection.getConnection()))
                STATE.send(packet, PacketDistributor.PLAYER.with(player));
        }
        public void sendToTrackingAndSelf(ServerPlayer player,Entity dog,CustomPacketPayload packet){
            var recipients=new java.util.LinkedHashSet<ServerPlayer>();
            // 1.21.1 has no public per-entity watcher iterator. Chunk watchers are a safe superset;
            // clients render only the UUIDs of entities they currently know.
            if(player.level() instanceof net.minecraft.server.level.ServerLevel level) recipients.addAll(level.getChunkSource().chunkMap.getPlayers(player.chunkPosition(),false));
            if(dog.level() instanceof net.minecraft.server.level.ServerLevel level) recipients.addAll(level.getChunkSource().chunkMap.getPlayers(dog.chunkPosition(),false));
            recipients.add(player); recipients.forEach(recipient->sendToPlayer(recipient,packet));
        }
    }
}
