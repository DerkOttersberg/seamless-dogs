package io.github.derkottersberg.seamlessdogs.client;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.internal.ClientPlatformServices;
import io.github.derkottersberg.seamlessdogs.network.PetRequest;
import io.github.derkottersberg.seamlessdogs.network.PetState;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.EntityHitResult;

public final class DogsClient {
    private static ClientPlatformServices platform;
    private static final Map<UUID, Clip> players = new HashMap<>();
    private static Object previousLevel;
    private static long nextRequest;
    public static void initialize(ClientPlatformServices services) { platform = services; ClientOptions.load(services.configDirectory()); }
    public static void receive(PetState state) {
        var client = Minecraft.getInstance();
        if (client.level == null) return;
        if (state.remainingTicks() == 0) players.remove(state.player());
        else players.put(state.player(), new Clip(state.dog(), client.level.getGameTime() - (SeamlessDogs.DURATION - state.remainingTicks())));
    }
    public static void tick(Minecraft client) {
        if (previousLevel != client.level) { players.clear(); nextRequest = 0; previousLevel = client.level; }
        if (client.level == null || client.player == null) return;
        players.values().removeIf(clip -> client.level.getGameTime() - clip.start >= SeamlessDogs.DURATION);
        while (platform.petKey().consumeClick()) {
            Wolf dog = target();
            if (client.gui.screen() == null && dog != null && client.level.getGameTime() >= nextRequest) {
                platform.sendToServer(new PetRequest(dog.getId()));
                nextRequest = client.level.getGameTime() + SeamlessDogs.COOLDOWN;
            }
        }
    }
    public static Wolf target() {
        var c = Minecraft.getInstance();
        if (platform == null || !platform.serverSupportsPetting() || c.player == null || c.level == null
            || !c.player.getMainHandItem().isEmpty() || c.player.isSpectator() || c.player.isUsingItem() || c.player.isPassenger()) return null;
        if (c.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Wolf dog && dog.isTame()
            && dog.isOwnedBy(c.player) && dog.isAlive() && !dog.isAngry() && dog.getTarget() == null
            && c.player.distanceToSqr(dog) <= SeamlessDogs.REACH * SeamlessDogs.REACH) return dog;
        return null;
    }
    public static PetAnimation.Sample playerSample(int id, float partial) {
        var c = Minecraft.getInstance();
        if (c.level == null || c.level.getEntity(id) == null) return PetAnimation.sample(-1);
        Clip clip = players.get(c.level.getEntity(id).getUUID());
        return sample(clip, partial);
    }
    public static PetAnimation.Sample dogSample(UUID dog, float partial) {
        Clip clip = players.values().stream().filter(p -> p.dog.equals(dog)).findFirst().orElse(null);
        return sample(clip, partial);
    }
    private static PetAnimation.Sample sample(Clip clip, float partial) {
        var c = Minecraft.getInstance();
        return clip == null || c.level == null ? PetAnimation.sample(-1) : PetAnimation.sample(c.level.getGameTime() - clip.start + partial);
    }
    public static void prompt(GuiGraphicsExtractor graphics) {
        var c = Minecraft.getInstance();
        if (!ClientOptions.prompt || c.gui.hud.isHidden() || c.gui.screen() != null || target() == null) return;
        boolean active = players.containsKey(c.player.getUUID());
        boolean cooldown = c.level.getGameTime() < nextRequest;
        Component text = Component.translatable(active ? "seamlessdogs.petting" : cooldown ? "seamlessdogs.cooldown" : "seamlessdogs.prompt", platform.petKey().getTranslatedKeyMessage());
        int width = c.font.width(text);
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() - 58;
        graphics.fill(x - width / 2 - 7, y - 4, x + width / 2 + 7, y + 13, 0xB0182125);
        graphics.centeredText(c.font, text, x, y, active ? 0xFFB7E8BD : 0xFFF4EEE4);
    }
    private record Clip(UUID dog, long start) { }
}
