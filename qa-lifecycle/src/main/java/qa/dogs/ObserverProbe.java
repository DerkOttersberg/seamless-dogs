package qa.dogs;

import io.github.derkottersberg.seamlessdogs.client.DogsClient;
import io.github.derkottersberg.seamlessdogs.network.PetRequest;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.wolf.Wolf;

/** A second real client verifies tracking, remote rendering and active disconnect. */
public final class ObserverProbe {
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
            var direction = dog.getEyePosition().subtract(client.player.getEyePosition());
            client.player.setYRot((float)Math.toDegrees(Math.atan2(-direction.x, direction.z)));
            client.player.setXRot((float)-Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x*direction.x+direction.z*direction.z))));
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
            if (++disconnected > 10 && lateTracking && dog != null && !dog.isRemoved()) {
                if (DogsClient.dogSample(dog.getUUID(), 0).weight() != 0)
                    throw new IllegalStateException("Disconnected owner's clip remained active");
                try {
                    Files.writeString(client.gameDirectory.toPath().resolve("dogs-observer-passed.txt"),
                        "PASS two real clients: ownership prompt hidden; remote arm/dog/eyes/entity sound; late tracking; active owner disconnect.\n");
                } catch (Exception e) { throw new RuntimeException(e); }
                finished = true; ClientProbe.log("DOGS_OBSERVER_PASS");
            }
        }
    }
}
