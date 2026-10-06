package qa.dogs;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.derkottersberg.seamlessdogs.client.*;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;

/** Test-only genuine dimension, death/respawn, menu and same-JVM reconnect. */
public final class LifecycleProbe {
    private int phase, ticks, total, shutdown;
    private boolean finished;
    private java.util.UUID dogUuid,catUuid;
    public void tick(Minecraft client) {
        if (finished) { if (++shutdown == 30) client.stop(); return; }
        if (++total > 6000) throw new IllegalStateException("Lifecycle timed out phase=" + phase);
        ticks++;
        if (phase == 11 || phase == 24) {
            if (client.level == null && ticks > 20) {
                if (DogsClient.dogSample(dogUuid, 0).weight() != 0) throw new IllegalStateException("Disconnected clip retained");
                if(catUuid!=null&&!DogsClient.actionPose(catUuid,0).parts().isEmpty())throw new IllegalStateException("Disconnected idle clip retained");
                String address = System.getProperty("qa.serverAddress");
                ServerData data = new ServerData("Dogs lifecycle", address, ServerData.Type.OTHER);
                ConnectScreen.startConnecting(new TitleScreen(), client, ServerAddress.parseString(address), data, false, null);
                next();
            }
            return;
        }
        if (client.player == null || client.level == null) return;
        if ((phase == 0 || phase == 3 || phase == 7 || phase == 9 || phase == 13)
            && (client.screen != null || client.getOverlay() != null)) return;
        client.options.pauseOnLostFocus = false;
        Wolf dog = null;
        for (var entity : client.level.entitiesForRendering()) if (entity instanceof Wolf wolf && wolf.isOwnedBy(client.player)) { dog = wolf; break; }
        if (dog != null) {
            dogUuid = dog.getUUID();
            var direction = dog.getEyePosition().subtract(client.player.getEyePosition());
            client.player.setYRot((float)Math.toDegrees(Math.atan2(-direction.x, direction.z)));
            client.player.setXRot((float)-Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x*direction.x+direction.z*direction.z))));
        }
        float weight = DogsClient.playerSample(client.player.getId(), 0).weight();
        switch (phase) {
            case 0 -> { if (ticks > 40 && dog != null && DogsClient.target() == dog) { click(); next(); } }
            case 1 -> { if (weight > .2F) { command(client, "dimension"); next(); } }
            case 2 -> {
                if (client.level.dimension() == Level.NETHER && ticks > 15) {
                    assertClear(client, "dimension"); ClientProbe.log("DOGS_LIFECYCLE_DIMENSION_PASS");
                    command(client, "home"); next();
                }
            }
            case 3 -> { if (client.level.dimension() == Level.OVERWORLD && ticks > 80 && dog != null && DogsClient.target() == dog) { click(); next(); } }
            case 4 -> { if (weight > .2F) { command(client, "die"); next(); } }
            case 5 -> {
                if (!client.player.isAlive() && ticks > 15) {
                    assertClear(client, "death"); client.player.respawn(); next();
                }
            }
            case 6 -> {
                if (client.player.isAlive() && ticks > 30) {
                    assertClear(client, "respawn"); ClientProbe.log("DOGS_LIFECYCLE_RESPAWN_PASS");
                    command(client, "home"); next();
                }
            }
            case 7 -> { if (ticks > 80 && dog != null && DogsClient.target() == dog) { click(); next(); } }
            case 8 -> {
                if (weight > .2F && ticks < 50) { client.setScreen(new DogsSettingsScreen(null)); ticks = 50; }
                if (ticks == 130) click();
                if (ticks > 155) {
                    assertClear(client, "menu keypress"); ClientProbe.log("DOGS_LIFECYCLE_MENU_PASS");
                    client.setScreen(null); next();
                }
            }
            case 9 -> { if (ticks > 15 && dog != null && DogsClient.target() == dog) { click(); next(); } }
            case 10 -> {
                if (weight > .2F) {
                    client.getConnection().getConnection().disconnect(Component.literal("Isolated lifecycle reconnect"));
                    client.disconnect(new TitleScreen()); next();
                }
            }
            case 12 -> { if (ticks > 30) { assertClear(client, "reconnect before request"); command(client, "home"); next(); } }
            case 13 -> { if (ticks > 80 && dog != null && DogsClient.target() == dog) { click(); next(); } }
            case 14 -> {
                if (weight > .2F) {
                    ClientProbe.log("DOGS_LIFECYCLE_RECONNECT_PASS");
                    next();
                }
            }
            case 15 -> { if(ticks>65){command(client,"cat_idle");next();} }
            case 16,19,23,27 -> {
                for(var entity:client.level.entitiesForRendering())if(entity instanceof net.minecraft.world.entity.animal.Cat pet&&pet.isOwnedBy(client.player))catUuid=pet.getUUID();
                if(catUuid!=null&&!DogsClient.actionPose(catUuid,0).parts().isEmpty()){
                    if(phase==16)command(client,"dimension");
                    else if(phase==19)command(client,"die");
                    else if(phase==23){client.getConnection().getConnection().disconnect(Component.literal("Isolated idle reconnect"));client.disconnect(new TitleScreen());}
                    else {
                        ClientProbe.log("DOGS_LIFECYCLE_CAT_RECONNECT_PASS");
                        try { Files.writeString(client.gameDirectory.toPath().resolve("dogs-lifecycle-passed.txt"), "PASS actual dimension, death/respawn, menu key rejection and active same-JVM reconnect; new request accepted afterwards; chest idle cleanup on dimension, death/respawn and same-JVM reconnect.\n"); }
                    catch (Exception e) { throw new RuntimeException(e); }
                    finished = true; ClientProbe.log("DOGS_LIFECYCLE_PASS");
                    }
                    next();
                }
            }
            case 17 -> {if(client.level.dimension()==Level.NETHER&&ticks>20){assertClear(client,"cat dimension");ClientProbe.log("DOGS_LIFECYCLE_CAT_DIMENSION_PASS");command(client,"home");next();}}
            case 18,22,26 -> {if(client.level.dimension()==Level.OVERWORLD&&ticks>80){command(client,"cat_idle");next();}}
            case 20 -> {if(!client.player.isAlive()&&ticks>20){assertClear(client,"cat death");client.player.respawn();next();}}
            case 21 -> {if(client.player.isAlive()&&ticks>30){assertClear(client,"cat respawn");ClientProbe.log("DOGS_LIFECYCLE_CAT_RESPAWN_PASS");command(client,"home");next();}}
            case 25 -> {if(ticks>30){assertClear(client,"cat reconnect");command(client,"home");next();}}
        }
    }
    private void assertClear(Minecraft client, String cause) {
        if(catUuid!=null&&!DogsClient.actionPose(catUuid,0).parts().isEmpty())throw new IllegalStateException("Stale idle after "+cause);
        if (DogsClient.playerSample(client.player.getId(), 0).weight() != 0 || dogUuid != null && DogsClient.dogSample(dogUuid, 0).weight() != 0)
            throw new IllegalStateException("Stale petting after " + cause);
    }
    private void click() { KeyMapping.click(InputConstants.getKey(DogsKeys.PET.saveString())); }
    private void command(Minecraft client, String name) { client.getConnection().sendCommand("dogsqa " + name); }
    private void next() { phase++; ticks = 0; ClientProbe.log("lifecycle phase=" + phase); }
}
