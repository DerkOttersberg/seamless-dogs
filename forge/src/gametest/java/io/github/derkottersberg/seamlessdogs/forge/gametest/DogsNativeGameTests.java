package io.github.derkottersberg.seamlessdogs.forge.gametest;
import io.github.derkottersberg.seamlessdogs.gametest.DogsScenarios;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
@Mod("seamlessdogstests")
public final class DogsNativeGameTests {
    public DogsNativeGameTests(FMLJavaModLoadingContext context) {
        RegisterEvent.getBus(context.getModBusGroup()).addListener(event -> {
            if (event.getRegistryKey() != Registries.TEST_FUNCTION) return;
            event.register(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "ownercanpet"), () -> DogsScenarios::ownerCanPet);
            event.register(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "rejectinvalidrequests"), () -> DogsScenarios::rejectInvalidRequests);
            event.register(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "cooldownandcancellation"), () -> DogsScenarios::cooldownAndCancellation);
            event.register(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("seamlessdogs", "codecroundtrip"), () -> DogsScenarios::codecRoundTrip);
        });
    }
}
