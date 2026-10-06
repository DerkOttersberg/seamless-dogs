package io.github.derkottersberg.seamlessdogs.fabric.gametest;
import io.github.derkottersberg.seamlessdogs.gametest.DogsScenarios;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
public final class DogsGameTests {
    @GameTest(maxTicks = 80) public void ownerCanPet(GameTestHelper helper) { DogsScenarios.ownerCanPet(helper); }
    @GameTest(maxTicks = 80) public void rejectInvalidRequests(GameTestHelper helper) { DogsScenarios.rejectInvalidRequests(helper); }
    @GameTest(maxTicks = 80) public void cooldownAndCancellation(GameTestHelper helper) { DogsScenarios.cooldownAndCancellation(helper); }
    @GameTest(maxTicks = 80) public void codecRoundTrip(GameTestHelper helper) { DogsScenarios.codecRoundTrip(helper); }
    @GameTest(maxTicks = 80) public void catOwnership(GameTestHelper helper) { DogsScenarios.catOwnership(helper); }
    @GameTest(maxTicks = 80) public void diggingTerrain(GameTestHelper helper) { DogsScenarios.diggingTerrain(helper); }
    @GameTest(maxTicks = 80) public void v2CodecRoundTrip(GameTestHelper helper) { DogsScenarios.v2CodecRoundTrip(helper); }
    @GameTest(maxTicks = 300) public void idleContracts(GameTestHelper helper) { DogsScenarios.idleContracts(helper); }
}
