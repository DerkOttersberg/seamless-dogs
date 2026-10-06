package io.github.derkottersberg.seamlessdogs.neoforge.gametest;
import io.github.derkottersberg.seamlessdogs.gametest.DogsScenarios;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
@Mod("seamlessdogstests")
public final class DogsNativeGameTests {
    public DogsNativeGameTests(IEventBus bus) {
        var functions = DeferredRegister.<Consumer<GameTestHelper>>create(Registries.TEST_FUNCTION, "seamlessdogs");
        functions.register("ownercanpet", () -> DogsScenarios::ownerCanPet);
        functions.register("rejectinvalidrequests", () -> DogsScenarios::rejectInvalidRequests);
        functions.register("cooldownandcancellation", () -> DogsScenarios::cooldownAndCancellation);
        functions.register("codecroundtrip", () -> DogsScenarios::codecRoundTrip);
        functions.register("catownership", () -> DogsScenarios::catOwnership);
        functions.register("diggingterrain", () -> DogsScenarios::diggingTerrain);
        functions.register("v2codecroundtrip", () -> DogsScenarios::v2CodecRoundTrip);
        functions.register("idlecontracts", () -> DogsScenarios::idleContracts);
        functions.register(bus);
        bus.addListener((RegisterGameTestsEvent event) -> {
            var environment = event.registerEnvironment(Identifier.fromNamespaceAndPath("seamlessdogs", "test_environment"), new TestEnvironmentDefinition.AllOf());
            event.registerTest(Identifier.fromNamespaceAndPath("seamlessdogs", "ownercanpet"), new FunctionGameTestInstance(net.minecraft.resources.ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "ownercanpet")), new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 80, 0, true)));
            event.registerTest(Identifier.fromNamespaceAndPath("seamlessdogs", "rejectinvalidrequests"), new FunctionGameTestInstance(net.minecraft.resources.ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "rejectinvalidrequests")), new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 80, 0, true)));
            event.registerTest(Identifier.fromNamespaceAndPath("seamlessdogs", "cooldownandcancellation"), new FunctionGameTestInstance(net.minecraft.resources.ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "cooldownandcancellation")), new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 80, 0, true)));
            event.registerTest(Identifier.fromNamespaceAndPath("seamlessdogs", "codecroundtrip"), new FunctionGameTestInstance(net.minecraft.resources.ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "codecroundtrip")), new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 80, 0, true)));
            event.registerTest(Identifier.fromNamespaceAndPath("seamlessdogs", "catownership"), new FunctionGameTestInstance(net.minecraft.resources.ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "catownership")), new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 80, 0, true)));
            event.registerTest(Identifier.fromNamespaceAndPath("seamlessdogs", "diggingterrain"), new FunctionGameTestInstance(net.minecraft.resources.ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "diggingterrain")), new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 80, 0, true)));
            event.registerTest(Identifier.fromNamespaceAndPath("seamlessdogs", "v2codecroundtrip"), new FunctionGameTestInstance(net.minecraft.resources.ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "v2codecroundtrip")), new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 80, 0, true)));
            event.registerTest(Identifier.fromNamespaceAndPath("seamlessdogs", "idlecontracts"), new FunctionGameTestInstance(net.minecraft.resources.ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "idlecontracts")), new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 300, 0, true)));
        });
    }
}
