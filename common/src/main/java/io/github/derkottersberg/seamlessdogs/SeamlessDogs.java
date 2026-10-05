package io.github.derkottersberg.seamlessdogs;

import io.github.derkottersberg.seamlessdogs.internal.PlatformServices;
import io.github.derkottersberg.seamlessdogs.network.PetRequest;
import io.github.derkottersberg.seamlessdogs.network.PetState;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.wolf.Wolf;

/** All state changes and authorization run on the logical server thread. */
public final class SeamlessDogs {
    public static final int DURATION = 40;
    public static final int COOLDOWN = 60;
    public static final double REACH = 3.0;
    private static PlatformServices platform;
    private static final Map<UUID, Session> sessions = new HashMap<>();
    private static final Map<UUID, Long> nextAllowed = new HashMap<>();
    private SeamlessDogs() { }
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath("seamlessdogs", path); }
    public static void initialize(PlatformServices services) { platform = services; }

    public static boolean canPet(ServerPlayer player, Wolf dog) {
        return player.isAlive() && !player.isSpectator() && !player.isPassenger() && !player.isUsingItem()
            && player.getMainHandItem().isEmpty() && dog.isAlive() && dog.isTame() && dog.isOwnedBy(player)
            && !dog.isAngry() && dog.getTarget() == null && player.level() == dog.level()
            && player.distanceToSqr(dog) <= REACH * REACH && player.hasLineOfSight(dog);
    }
    public static boolean request(ServerPlayer player, PetRequest request) {
        if (request.dogId() < 0) return false;
        Entity entity = player.level().getEntity(request.dogId()); // No chunk loading.
        if (!(entity instanceof Wolf dog) || !canPet(player, dog)) return false;
        long now = player.level().getGameTime();
        if (sessions.containsKey(player.getUUID()) || nextAllowed.getOrDefault(player.getUUID(), 0L) > now
            || sessions.values().stream().anyMatch(s -> s.dog == dog)) return false;
        Session session = new Session(player, dog, now);
        sessions.put(player.getUUID(), session);
        nextAllowed.put(player.getUUID(), now + COOLDOWN);
        dog.getNavigation().stop();
        dog.getLookControl().setLookAt(player, 30, 30);
        var variant = dog.get(DataComponents.WOLF_SOUND_VARIANT);
        if (variant != null) {
            var sounds = dog.isBaby() ? variant.value().babySounds() : variant.value().adultSounds();
            // Entity-bound sound packet follows the dog, preserving each wolf's voice.
            dog.level().playSeededSound(null, dog, sounds.pantSound(), SoundSource.NEUTRAL, 0.65F, 1.05F, dog.getRandom().nextLong());
        }
        platform.sendToTrackingAndSelf(player, dog, state(session, DURATION));
        return true;
    }
    public static void tick(MinecraftServer server) {
        var iterator = sessions.values().iterator();
        while (iterator.hasNext()) {
            Session s = iterator.next();
            if (s.player.level().getGameTime() - s.start >= DURATION || !canPet(s.player, s.dog) || s.dog.isRemoved()) {
                platform.sendToTrackingAndSelf(s.player, s.dog, state(s, 0));
                iterator.remove();
            } else {
                s.dog.getNavigation().stop();
                s.dog.getLookControl().setLookAt(s.player, 30, 30);
            }
        }
        nextAllowed.entrySet().removeIf(e -> server.overworld().getGameTime() >= e.getValue() && !sessions.containsKey(e.getKey()));
    }
    public static void disconnect(ServerPlayer player) {
        Session s = sessions.remove(player.getUUID());
        if (s != null) platform.sendToTrackingAndSelf(player, s.dog, state(s, 0));
        nextAllowed.remove(player.getUUID());
    }
    public static void syncTo(ServerPlayer observer, Entity entity) {
        for (Session s : sessions.values()) {
            if (entity == s.dog || entity == s.player) {
                int remaining = (int) Math.clamp(DURATION - (s.player.level().getGameTime() - s.start), 0, DURATION);
                platform.sendToPlayer(observer, state(s, remaining));
            }
        }
    }
    public static boolean isPetting(UUID player) { return sessions.containsKey(player); }
    public static void clear() { sessions.clear(); nextAllowed.clear(); }
    private static PetState state(Session s, int remaining) { return new PetState(s.player.getUUID(), s.dog.getUUID(), remaining); }
    private record Session(ServerPlayer player, Wolf dog, long start) { }
}
