package io.github.derkottersberg.seamlessdogs.gameplay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.slf4j.LoggerFactory;

/** World-local rules, owner preferences and persistent cooldowns. Never rewrites a damaged original. */
public final class PetWorldState {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private Data data = new Data();
    private boolean writable = true, dirty;
    public PetWorldState(Path world) {
        file = world.resolve("serverconfig/seamlessdogs-world.json");
        if (Files.exists(file)) {
            try {
                Data loaded = JSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Data.class);
                if (loaded == null || loaded.schema != 1 || loaded.ownerDigging == null
                    || loaded.nextActions == null || loaded.ownerNextDig == null || loaded.nextReactions == null || loaded.revision < 0)
                    throw new IOException("Unsupported or invalid world settings");
                loaded.nextActions.entrySet().removeIf(e -> e.getValue() == null || e.getValue() < 0);
                loaded.ownerNextDig.entrySet().removeIf(e -> e.getValue() == null || e.getValue() < 0);
                loaded.nextReactions.entrySet().removeIf(e -> e.getValue() == null || e.getValue() < 0);
                loaded.ownerDigging.values().removeIf(Objects::isNull);
                data = loaded;
            } catch (IOException | RuntimeException exception) {
                writable = false; data.digging = data.finds = data.stretching = false;
                LoggerFactory.getLogger("SeamlessDogs").error("Preserving unreadable pet settings {}; digging disabled", file, exception);
            }
        }
    }
    public boolean writable() { return writable; }
    public boolean digging() { return data.digging; }
    public boolean finds() { return data.finds; }
    public boolean stretching() { return data.stretching; }
    public long revision() { return data.revision; }
    public boolean ownerDigging(UUID owner) { return data.ownerDigging.getOrDefault(owner.toString(), true); }
    public long due(UUID pet) { return data.nextActions.getOrDefault(pet.toString(), -1L); }
    public void schedule(UUID pet, long due) { data.nextActions.put(pet.toString(), due); dirty = true; }
    public long reactionDue(UUID pet) { return data.nextReactions.getOrDefault(pet.toString(), -1L); }
    public void scheduleReaction(UUID pet,long due) { data.nextReactions.put(pet.toString(),due);dirty=true; }
    public boolean ownerReady(UUID owner, long now) { return data.ownerNextDig.getOrDefault(owner.toString(), 0L) <= now; }
    public void dug(UUID owner, long now) { data.ownerNextDig.put(owner.toString(), now + 12000); dirty = true; }
    public boolean update(UUID owner, boolean personal, int flags, long expectedRevision) {
        if (!writable || expectedRevision != data.revision) return false;
        Data previous = data;
        data = JSON.fromJson(JSON.toJson(previous), Data.class);
        if (personal) data.ownerDigging.put(owner.toString(), (flags & 1) != 0);
        else { data.digging = (flags & 1) != 0; data.finds = (flags & 2) != 0; data.stretching = (flags & 4) != 0; }
        data.revision++;
        dirty = true;
        if (save()) return true;
        data = previous;
        return false;
    }
    public boolean save() {
        if (!writable) return false;
        if (!dirty) return true;
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(temporary, JSON.toJson(data), StandardCharsets.UTF_8);
            if (Files.exists(file)) Files.copy(file, file.resolveSibling(file.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            try { Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
            dirty = false; return true;
        } catch (IOException exception) {
            LoggerFactory.getLogger("SeamlessDogs").error("Could not save pet settings {}", file, exception);
            return false;
        }
    }
    private static final class Data {
        int schema = 1;
        boolean digging = true, finds = true, stretching = true;
        long revision;
        Map<String, Boolean> ownerDigging = new TreeMap<>();
        Map<String, Long> nextActions = new TreeMap<>(), ownerNextDig = new TreeMap<>(), nextReactions = new TreeMap<>();
    }
}
