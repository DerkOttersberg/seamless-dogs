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
import net.minecraftforge.network.simple.SimpleChannel;
@Mod("seamlessdogs")
public final class DogsForge {
    private static final SimpleChannel NETWORK=NetworkRegistry.newSimpleChannel(SeamlessDogs.id("network"),()->"1",
        DogsForge::compatible,DogsForge::compatible);
    private static final SimpleChannel V2=NetworkRegistry.newSimpleChannel(SeamlessDogs.id("network_v2"),()->"2",
        value->"2".equals(value)||NetworkRegistry.ABSENT.equals(value)||NetworkRegistry.ACCEPTVANILLA.equals(value),
        value->"2".equals(value)||NetworkRegistry.ABSENT.equals(value)||NetworkRegistry.ACCEPTVANILLA.equals(value));
    private static boolean compatible(String value){return "1".equals(value)||NetworkRegistry.ABSENT.equals(value)||NetworkRegistry.ACCEPTVANILLA.equals(value);}
    public DogsForge(){
        NETWORK.registerMessage(0,PetRequest.class,(packet,buffer)->PetRequest.CODEC.encode(buffer,packet),PetRequest.CODEC::decode,(packet,supplier)-> {
            var context=supplier.get(); context.enqueueWork(()->{if(context.getSender()!=null) SeamlessDogs.request(context.getSender(),packet);}); context.setPacketHandled(true);
        },Optional.of(NetworkDirection.PLAY_TO_SERVER));
        NETWORK.registerMessage(1,PetState.class,(packet,buffer)->PetState.CODEC.encode(buffer,packet),PetState.CODEC::decode,(packet,supplier)->{
            var context=supplier.get(); context.enqueueWork(()->DogsForgeClient.receive(packet)); context.setPacketHandled(true);
        },Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        V2.registerMessage(0,PetControl.class,(packet,buffer)->PetControl.CODEC.encode(buffer,packet),PetControl.CODEC::decode,(packet,supplier)->{
            var context=supplier.get();context.enqueueWork(()->{if(context.getSender()!=null)SeamlessDogs.control(context.getSender(),packet);});context.setPacketHandled(true);
        },Optional.of(NetworkDirection.PLAY_TO_SERVER));
        V2.registerMessage(1,PetUpdate.class,(packet,buffer)->PetUpdate.CODEC.encode(buffer,packet),PetUpdate.CODEC::decode,(packet,supplier)->{
            var context=supplier.get();context.enqueueWork(()->DogsForgeClient.receive(packet));context.setPacketHandled(true);
        },Optional.of(NetworkDirection.PLAY_TO_CLIENT));
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
    static boolean supportsV2(Connection connection){return V2.isRemotePresent(connection);}
    static boolean supports(Connection connection){return NETWORK.isRemotePresent(connection);}
    static void send(DogsPayload packet){if(packet instanceof PetControl)V2.sendToServer(packet);else NETWORK.sendToServer(packet);}
    private static final class Services implements PlatformServices {
        public boolean supportsV2(ServerPlayer player){return player.connection!=null&&DogsForge.supportsV2(player.connection.connection);}
        public boolean mayDig(ServerPlayer owner,net.minecraft.world.entity.TamableAnimal pet,net.minecraft.core.BlockPos pos,boolean commit){return DogsForgeProtection.allowed(owner,pet,pos,commit);}
        public String protectionStatus(){return DogsForgeProtection.status();}
        public void sendToPlayer(ServerPlayer player,DogsPayload packet){if(packet instanceof PetUpdate){if(supportsV2(player))V2.send(PacketDistributor.PLAYER.with(()->player),packet);return;}if(supportsV2(player))return;if(player.connection!=null && NETWORK.isRemotePresent(player.connection.connection)) NETWORK.send(PacketDistributor.PLAYER.with(()->player),packet);}
        public void sendToTrackingAndSelf(ServerPlayer player,Entity dog,DogsPayload packet){
            var recipients=new java.util.LinkedHashSet<ServerPlayer>();
            // 1.20.1 has no public per-entity watcher iterator. Chunk watchers are a safe superset;
            // clients render only the UUIDs of entities they currently know.
            if(player.level() instanceof net.minecraft.server.level.ServerLevel level) recipients.addAll(level.getChunkSource().chunkMap.getPlayers(player.chunkPosition(),false));
            if(dog.level() instanceof net.minecraft.server.level.ServerLevel level) recipients.addAll(level.getChunkSource().chunkMap.getPlayers(dog.chunkPosition(),false));
            recipients.add(player); recipients.forEach(recipient->sendToPlayer(recipient,packet));
        }
    }
}
