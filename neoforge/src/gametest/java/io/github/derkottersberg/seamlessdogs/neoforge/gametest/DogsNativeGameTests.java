package io.github.derkottersberg.seamlessdogs.neoforge.gametest;
import io.github.derkottersberg.seamlessdogs.gametest.DogsScenarios;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.fml.common.Mod;
@Mod("seamlessdogstests")
@GameTestHolder("seamlessdogs")
@PrefixGameTestTemplate(false)
public final class DogsNativeGameTests {
    public DogsNativeGameTests(){ }
    @GameTest(template="empty",timeoutTicks=80) public static void ownerCanPet(GameTestHelper helper){DogsScenarios.ownerCanPet(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void rejectInvalidRequests(GameTestHelper helper){DogsScenarios.rejectInvalidRequests(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void cooldownAndCancellation(GameTestHelper helper){DogsScenarios.cooldownAndCancellation(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void codecRoundTrip(GameTestHelper helper){DogsScenarios.codecRoundTrip(helper);}
}
