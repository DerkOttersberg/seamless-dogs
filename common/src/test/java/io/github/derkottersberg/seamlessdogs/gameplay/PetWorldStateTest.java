package io.github.derkottersberg.seamlessdogs.gameplay;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class PetWorldStateTest {
    @TempDir Path directory;
    @Test void ownerPreferencesRulesAndCooldownsSurviveRestart() {
        UUID owner=UUID.randomUUID(),other=UUID.randomUUID(),pet=UUID.randomUUID();
        var state=new PetWorldState(directory);
        assertTrue(state.update(owner,true,0,0));
        assertFalse(state.ownerDigging(owner)); assertTrue(state.ownerDigging(other));
        assertTrue(state.update(owner,false,4,1));
        state.schedule(pet,12345);state.scheduleReaction(pet,1800);state.dug(owner,1200);assertTrue(state.save());
        var restored=new PetWorldState(directory);
        assertEquals(12345,restored.due(pet));assertFalse(restored.ownerReady(owner,13199));assertTrue(restored.ownerReady(owner,13200));
        assertEquals(1800,restored.reactionDue(pet));
        assertFalse(restored.digging());assertFalse(restored.finds());assertTrue(restored.stretching());assertFalse(restored.ownerDigging(owner));
        assertTrue(Files.exists(directory.resolve("serverconfig/seamlessdogs-world.json.bak")));
    }
    @Test void staleDraftCannotOverwriteAnotherAdministratorsChange() {
        var state=new PetWorldState(directory);UUID owner=UUID.randomUUID();
        assertTrue(state.update(owner,false,0,0));assertFalse(state.update(owner,false,7,0));assertFalse(state.digging());
        assertEquals(1,state.revision());
    }
    @Test void corruptedFileIsPreservedAndDiggingFailsClosed() throws Exception {
        Path file=directory.resolve("serverconfig/seamlessdogs-world.json");Files.createDirectories(file.getParent());Files.writeString(file,"broken original");
        var state=new PetWorldState(directory);assertFalse(state.writable());assertFalse(state.digging());assertFalse(state.update(UUID.randomUUID(),false,7,0));
        assertEquals("broken original",Files.readString(file));
    }
    @Test void previousWorldWithoutReactionMapKeepsItsExistingPreferences() throws Exception {
        UUID owner=UUID.randomUUID();Path file=directory.resolve("serverconfig/seamlessdogs-world.json");Files.createDirectories(file.getParent());
        Files.writeString(file,"{\"schema\":1,\"revision\":3,\"digging\":false,\"stretching\":true,\"ownerDigging\":{\""+owner+"\":false},\"nextActions\":{},\"ownerNextDig\":{}}");
        var state=new PetWorldState(directory);assertTrue(state.writable());assertFalse(state.digging());assertFalse(state.ownerDigging(owner));assertEquals(3,state.revision());
        assertEquals(-1,state.reactionDue(UUID.randomUUID()));
    }
}
