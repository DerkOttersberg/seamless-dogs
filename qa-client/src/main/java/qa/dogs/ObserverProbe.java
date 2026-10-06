package qa.dogs;

import io.github.derkottersberg.seamlessdogs.client.DogsClient;
import io.github.derkottersberg.seamlessdogs.network.PetRequest;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.Wolf;

/** A second real client verifies tracking, remote rendering and active disconnect. */
public final class ObserverProbe {
    private record Tracked(io.github.derkottersberg.seamlessdogs.network.PetUpdate state,int tick){}
    private static final java.util.Map<java.util.UUID,Tracked> tracked=new java.util.HashMap<>();
    private static final java.util.Set<String> captured=new java.util.HashSet<>();
    private static final java.util.Map<java.util.UUID,Integer> renderedTick=new java.util.HashMap<>();
    private static java.util.UUID pendingPet;
    private java.util.UUID previousLook;
    private net.minecraft.world.phys.Vec3 previousPosition;
    private int stableTicks;
    public static void rendered(java.util.UUID pet){var c=Minecraft.getInstance();if(c.player!=null)renderedTick.put(pet,c.player.tickCount);}
    public static void renderedAt(double x,double y,double z){
        if(!"observer".equals(System.getProperty("qa.role")))return;
        var c=Minecraft.getInstance();if(c.level==null)return;
        for(var entity:c.level.entitiesForRendering())if(entity.position().distanceToSqr(x,y,z)<.05)rendered(entity.getUUID());
    }
    public static void captureAfterFrame(Minecraft c){
        if(!"observer".equals(System.getProperty("qa.role"))||pendingPet==null||c.player==null||c.level==null)return;
        var id=pendingPet;pendingPet=null;
        if(renderedTick.getOrDefault(id,-1)!=c.player.tickCount)return;
        for(var pet:c.level.entitiesForRendering())if(pet.getUUID().equals(id)){capture(c,pet);return;}
    }
    public static void receive(io.github.derkottersberg.seamlessdogs.network.PetUpdate state) {
        if(!"observer".equals(System.getProperty("qa.role"))||state.action()==5)return;
        var c=Minecraft.getInstance();if(c.player==null)return;
        if(state.action()==0){tracked.remove(state.pet());return;}
        var previous=tracked.get(state.pet());
        if(previous==null||previous.state.sequence()!=state.sequence())tracked.put(state.pet(),new Tracked(state,c.player.tickCount));
    }
    private static void capture(Minecraft client,net.minecraft.world.entity.Entity pet) {
        var entry=tracked.get(pet.getUUID());if(entry==null)return;
        int elapsed=entry.state.elapsed()+client.player.tickCount-entry.tick;
        if(elapsed<18||elapsed>58)return;
        boolean baby=pet instanceof net.minecraft.world.entity.AgeableMob mob&&mob.isBaby();
        String name="observer-action-"+entry.state.action()+"-"+(baby?"baby":"adult")+"-"+(elapsed<36?"early":"held")+".png";
        if(!captured.add(name))return;
        screenshot(client,name);
    }

    private int ticks, disconnected, shutdown;
    private boolean initial, lostTracking, lateTracking, finished;
    private Wolf dog;
    public void tick(Minecraft client) {
        if (finished) { if (++shutdown == 30) client.stop(); return; }
        if (++ticks > 6000) throw new IllegalStateException("Observer timed out");
        if (client.player == null || client.level == null) return;
        client.options.pauseOnLostFocus = false;
        var owner = client.level.players().stream().filter(player -> player.getName().getString().equals("DogQA")).findFirst().orElse(null);
        if (owner != null) {
            disconnected = 0;
            if (dog == null || dog.isRemoved()) {
                if (initial && dog != null && !lostTracking) {
                    lostTracking = true;
                    ClientProbe.wolves = ClientProbe.playerModels = ClientProbe.expressiveEyes = 0;
                }
                dog = null;
                for (var entity : client.level.entitiesForRendering())
                    if (entity instanceof Wolf wolf && wolf.isOwnedBy(owner)) { dog = wolf; break; }
            }
            if (dog == null) return;
            net.minecraft.world.entity.Entity look=dog;
            for(var entity:client.level.entitiesForRendering())if(entity instanceof net.minecraft.world.entity.animal.Cat cat && !DogsClient.actionPose(cat.getUUID(),0).parts().isEmpty())look=cat;

            var direction = look.getEyePosition().subtract(client.player.getEyePosition());
            client.player.setYRot((float)Math.toDegrees(Math.atan2(-direction.x, direction.z)));
            client.player.setXRot((float)-Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x*direction.x+direction.z*direction.z))));
            boolean settled=look.getUUID().equals(previousLook)&&previousPosition!=null
                &&client.player.position().distanceToSqr(previousPosition)<.01;
            stableTicks=settled?stableTicks+1:0;previousLook=look.getUUID();previousPosition=client.player.position();
            if(stableTicks>=2)pendingPet=look.getUUID();
            if (DogsClient.target() != null || ClientProbe.prompts != 0)
                throw new IllegalStateException("Observer received an ownership prompt");
            if (DogsClient.playerSample(client.player.getId(), 0).weight() != 0)
                throw new IllegalStateException("Server accepted the observer's unauthorized request");
            if (DogsClient.dogSample(dog.getUUID(), 0).weight() > 0.2F
                && ClientProbe.wolves > 0 && ClientProbe.playerModels > 0 && ClientProbe.expressiveEyes > 0
                && ClientProbe.sounds > 0 && ClientProbe.soundDog == dog.getId()) {
                if (!initial) {
                    initial = true;
                    var platform = qa.dogs.mixin.ClientServicesProbe.qa$platform();
                    platform.sendToServer(new PetRequest(-1));
                    platform.sendToServer(new PetRequest(Integer.MAX_VALUE));
                    platform.sendToServer(new PetRequest(dog.getId()));
                    platform.sendToServer(new io.github.derkottersberg.seamlessdogs.network.PetControl(2,0,DogsClient.settings.revision()));
                    ClientProbe.log("DOGS_OBSERVER_RENDER_AND_SOUND_PASS; sent real unauthorized/unknown/negative C2S requests");
                }
                if (lostTracking && !lateTracking) { lateTracking = true; ClientProbe.log("DOGS_OBSERVER_LATE_TRACKING_PASS"); }
            }
        } else if (initial) {
            if (!lostTracking) {
                // At the far waypoint the owner and dog leave the client's tracker.
                lostTracking = true;
                ClientProbe.wolves = ClientProbe.playerModels = ClientProbe.expressiveEyes = 0;
            }
            if (++disconnected > 10 && PetFeatureProbe.catFrames>0 && PetFeatureProbe.catEyes>0 && PetFeatureProbe.catSounds>0
                && PetFeatureProbe.lateCats>0 && PetFeatureProbe.rejectedSettings>0
                && PetFeatureProbe.remoteDig>0 && PetFeatureProbe.remoteStretch>0 && lateTracking && dog != null && !dog.isRemoved()) {
                if(PetFeatureProbe.remoteKnead==0||PetFeatureProbe.remoteGroom==0||PetFeatureProbe.remoteTilt==0||PetFeatureProbe.lateExpressions<3||PetFeatureProbe.groomVariants!=4||PetFeatureProbe.renderedGroomVariants!=4||(PetFeatureProbe.lateGroomVariants&4)!=4)throw new IllegalStateException("New expressive clips or late synchronization missing");
                if (DogsClient.dogSample(dog.getUUID(), 0).weight() != 0)
                    throw new IllegalStateException("Disconnected owner's clip remained active");
                if(PetFeatureProbe.digSounds.getOrDefault("block.grass.hit",0)!=11||PetFeatureProbe.digSounds.getOrDefault("block.sand.hit",0)!=11)throw new IllegalStateException("Observer missing block-specific dog digging sounds: "+PetFeatureProbe.digSounds);
                try {
                    Files.writeString(client.gameDirectory.toPath().resolve("dogs-observer-passed.txt"),
                        "PASS two real clients: ownership prompt hidden; unauthorized pet/settings requests rejected; remote arm/dog/cat/eyes/entity sound; late dog/cat tracking; dig/stretch and biscuits/groom/ear tilt synchronization; three late expressive clips; active owner disconnect.\n");
                } catch (Exception e) { throw new RuntimeException(e); }
                for(int action:new int[]{1,2,3,4,6,7,8})if(captured.stream().noneMatch(name->name.startsWith("observer-action-"+action+"-")))throw new IllegalStateException("Missing observer capture for action "+action);
                SkinProbe.verify();finished = true; ClientProbe.log("DOGS_OBSERVER_PASS");
            }
        }
    }
    private static void screenshot(Minecraft client,String name){net.minecraft.client.Screenshot.grab(client.gameDirectory,name,client.getMainRenderTarget(),m->ClientProbe.log(m.getString()));}
}
