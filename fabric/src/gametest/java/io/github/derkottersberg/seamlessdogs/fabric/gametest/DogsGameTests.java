package io.github.derkottersberg.seamlessdogs.fabric.gametest;
import io.github.derkottersberg.seamlessdogs.gametest.DogsScenarios;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
public final class DogsGameTests implements FabricGameTest {
    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE,timeoutTicks=80) public void ownerCanPet(GameTestHelper helper) { DogsScenarios.ownerCanPet(helper); }
    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE,timeoutTicks=80) public void rejectInvalidRequests(GameTestHelper helper) { DogsScenarios.rejectInvalidRequests(helper); }
    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE,timeoutTicks=80) public void cooldownAndCancellation(GameTestHelper helper) { DogsScenarios.cooldownAndCancellation(helper); }
    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE,timeoutTicks=80) public void codecRoundTrip(GameTestHelper helper) { DogsScenarios.codecRoundTrip(helper); }
}
