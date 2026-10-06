package qa.dogs;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.derkottersberg.seamlessdogs.client.*;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.CameraType;
import net.minecraft.client.Screenshot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.HumanoidArm;

/** Drives the actual keybind and observes production state, render calls, and sound packets. */
public final class ClientProbe {
    public static int soundDog = -1, sounds, wolves, hands, playerModels, prompts, expressiveEyes, blinks;
    public static java.util.function.Function<net.minecraft.client.gui.screens.Screen, net.minecraft.client.gui.screens.Screen> settingsOpener = DogsSettingsScreen::new;
    private int phase, ticks, total, petStart, shutdown;
    private boolean finished;
    private Wolf dog;
    private ObserverProbe observer;
    private LifecycleProbe lifecycle;
    private boolean movedObserver;
    public void tick(Minecraft client) {
        if (System.getProperty("qa.role", "single").equals("lifecycle")) {
            if (lifecycle == null) lifecycle = new LifecycleProbe();
            lifecycle.tick(client); return;
        }
        if (System.getProperty("qa.role", "single").equals("observer")) {
            if (observer == null) observer = new ObserverProbe();
            observer.tick(client); return;
        }
        boolean multiplayer = System.getProperty("qa.role", "single").equals("owner");
        if (finished) {
            shutdown++;
            if (multiplayer && shutdown == 70) click();
            if (shutdown == (multiplayer ? 77 : 30)) client.stop();
            return;
        }
        if (++total > 6000) fail(client, "QA timed out phase=" + phase);
        if (client.player == null || client.level == null) return;
        if (multiplayer && phase == 0 && client.level.players().size() < 2) return;
        if (dog == null || dog.isRemoved()) {
            dog = client.level.entitiesForRendering().iterator().hasNext() ? findDog(client) : null;
            if (dog == null) return;
        }
        client.options.pauseOnLostFocus = false;
        client.options.renderDistance().set(3);
        client.options.simulationDistance().set(5);
        client.options.guiScale().set(Integer.getInteger("qa.guiScale", 2));
        var direction = dog.getEyePosition().subtract(client.player.getEyePosition());
        client.player.setYRot((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        client.player.setXRot((float) -Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z))));
        // A normal rear third-person view, angled enough to see both the dog and
        // the player's arm. This changes only the QA player's in-game look.
        if (phase == 3 && ticks > 3) { client.player.setYRot(client.player.getYRot() - 35); client.player.setXRot(20); }
        ticks++;
        if (phase == 0 && ticks % 200 == 0) log("Waiting for initial evidence: target=" + DogsClient.target()
            + " compatible=" + qa.dogs.mixin.ClientServicesProbe.qa$platform().serverSupportsPetting()
            + " owned=" + dog.isOwnedBy(client.player) + " hit=" + client.hitResult + " screen=" + client.gui.screen()
            + " prompt=" + prompts + " blink=" + blinks);
        if (client.gui.overlay() != null) return;
        var pose = DogsClient.playerSample(client.player.getId(), 0);
        switch (phase) {
            case 0 -> {
                if (ticks > 600) fail(client, "Initial prompt/blink missing: compatible="
                    + qa.dogs.mixin.ClientServicesProbe.qa$platform().serverSupportsPetting()
                    + " target=" + DogsClient.target() + " prompts=" + prompts + " blink=" + blinks);
                if (ticks > 40 && DogsClient.target() == dog && prompts > 0 && blinks > 0) {
                    capture(client, "01-prompt.png"); click(); next();
                }
            }
            case 1 -> {
                if (pose.weight() > 0.95F && hands > 0 && wolves > 0 && sounds > 0 && soundDog == dog.getId() && expressiveEyes > 0) {
                    capture(client, "02-first-person.png"); petStart = total; next();
                    log("PASS actual key request -> server state -> first person / dog rig / eyes / entity sound");
                }
                if (ticks > 150) fail(client, "First person evidence missing hands=" + hands + " wolves=" + wolves + " sounds=" + sounds + " eyes=" + expressiveEyes + " pose=" + pose);
            }
            case 2 -> {
                if (total - petStart > 65) { client.options.setCameraType(CameraType.THIRD_PERSON_BACK); playerModels = 0; click(); next(); }
            }
            case 3 -> {
                if (pose.weight() > 0.95F && playerModels > 0 && ticks > 12) { capture(client, "03-third-person.png"); petStart = total; next(); log("PASS third person production arm and dog rig"); }
                if (ticks > 150) fail(client, "Third person arm never rendered");
            }
            case 4 -> {
                if (total - petStart > 65) {
                    client.options.setCameraType(CameraType.FIRST_PERSON);
                    client.options.mainHand().set(HumanoidArm.LEFT); hands = 0; click(); next();
                }
            }
            case 5 -> {
                if (pose.weight() > 0.95F && hands > 0) { capture(client, "04-left-hand.png"); petStart = total; next(); log("PASS left handed first person"); }
                if (ticks > 150) fail(client, "Left hand never rendered");
            }
            case 6 -> {
                if (total - petStart > 65) { client.getConnection().sendCommand("dogsqa held"); next(); }
            }
            case 7 -> {
                if (ticks > 25) {
                    if (client.player.getMainHandItem().isEmpty() || DogsClient.target() != null || pose.weight() != 0) fail(client, "Held item hides prompt/cancels pose incorrectly");
                    click(); next();
                }
            }
            case 8 -> {
                if (ticks > 20) {
                    if (pose.weight() != 0) fail(client, "Pet request accepted with held item");
                    client.getConnection().sendCommand("dogsqa empty");
                    client.reloadResourcePacks(); next(); log("PASS occupied hand blocks interaction; starting resource reload");
                }
            }
            case 9 -> {
                if (ticks > 60 && client.gui.overlay() == null && DogsClient.target() == dog) { expressiveEyes = 0;
                    if (multiplayer && !movedObserver) {
                        client.getConnection().sendCommand("dogsqa observer_far"); movedObserver = true; ticks = 55; return;
                    }
                    click(); next(); }
            }
            case 10 -> {
                if (pose.weight() > 0.7F && expressiveEyes > 0) { capture(client, "05-after-reload.png");
                    if (multiplayer) client.getConnection().sendCommand("dogsqa observer_near");
                    petStart = total; next(); log("PASS eyes regenerated after resource reload"); }
                if (ticks > 150) fail(client, "Reload broke eyes");
            }
            case 11 -> {
                if (total - petStart > 50) { client.setScreenAndShow(settingsOpener.apply(null)); next(); }
            }
            case 12 -> {
                if (ticks > 15) { capture(client, "06-settings.png"); client.setScreenAndShow(null); next(); }
            }
            case 13 -> {
                if (ticks > 10) {
                    if (pose.weight() != 0) fail(client, "Pose did not return to vanilla");
                    client.getConnection().sendCommand("dogsqa puppy"); next();
                }
            }
            case 14 -> {
                if (ticks > 25 && dog.isBaby() && DogsClient.target() == dog) {
                    wolves = expressiveEyes = sounds = hands = 0; click(); next();
                }
                if (ticks > 150) fail(client, "Puppy fixture never synchronized");
            }
            case 15 -> {
                if (pose.weight() > 0.7F && wolves > 0 && expressiveEyes > 0 && sounds > 0 && hands > 0) {
                    capture(client, "07-puppy.png");
                    log("PASS puppy model, eyelids, hand and entity voice");
                    write(client, "PASS: packaged " + System.getProperty("qa.loader", "fabric") + " " + System.getProperty("qa.minecraft", "26.3") + "; real keybind/server packets; first/third/left-hand render; adult and puppy rig/eyes/voice; idle blink; occupied hand; resource reload; settings.\n");
                    finished = true; log("DOGS_CLIENT_PASS");
                }
                if (ticks > 150) fail(client, "Puppy feature missing rig=" + wolves + " eyes=" + expressiveEyes + " voice=" + sounds + " hands=" + hands);
            }
        }
    }
    private Wolf findDog(Minecraft c) { for (var entity : c.level.entitiesForRendering()) if (entity instanceof Wolf w && w.isOwnedBy(c.player)) return w; return null; }
    private void click() { KeyMapping.click(InputConstants.getKey(DogsKeys.PET.saveString())); }
    private void next() { phase++; ticks = 0; log("phase=" + phase); }
    private void capture(Minecraft c, String name) { Screenshot.grab(c.gameDirectory, name, c.gameRenderer.mainRenderTarget(), 1, message -> log(message.getString())); }
    private void write(Minecraft c, String value) { try { Files.writeString(c.gameDirectory.toPath().resolve("dogs-client-passed.txt"), value); } catch (Exception e) { throw new RuntimeException(e); } }
    private void fail(Minecraft c, String value) {
        try { Files.writeString(c.gameDirectory.toPath().resolve("dogs-client-failed.txt"), value); } catch (Exception ignored) { }
        throw new IllegalStateException(value);
    }
    public static void log(String value) { System.out.println("[DOGS-QA] " + value); }
}
