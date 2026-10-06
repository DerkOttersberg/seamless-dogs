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
    public DogsNativeGameTests(net.neoforged.bus.api.IEventBus bus){
        bus.addListener((net.neoforged.neoforge.event.AddPackFindersEvent event)->event.addPackFinders(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("seamlessdogstests","qa-pack"),
            net.minecraft.server.packs.PackType.SERVER_DATA,net.minecraft.network.chat.Component.literal("Deterministic native test loot"),
            net.minecraft.server.packs.repository.PackSource.BUILT_IN,true,net.minecraft.server.packs.repository.Pack.Position.TOP));
    }
    @GameTest(template="empty",timeoutTicks=80) public static void ownerCanPet(GameTestHelper helper){DogsScenarios.ownerCanPet(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void rejectInvalidRequests(GameTestHelper helper){DogsScenarios.rejectInvalidRequests(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void cooldownAndCancellation(GameTestHelper helper){DogsScenarios.cooldownAndCancellation(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void codecRoundTrip(GameTestHelper helper){DogsScenarios.codecRoundTrip(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void catOwnership(GameTestHelper helper){DogsScenarios.catOwnership(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void diggingTerrain(GameTestHelper helper){DogsScenarios.diggingTerrain(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void v2CodecRoundTrip(GameTestHelper helper){DogsScenarios.v2CodecRoundTrip(helper);}
    @GameTest(template="empty",timeoutTicks=270) public static void idleContracts(GameTestHelper helper){DogsScenarios.idleContracts(helper);}
}
