package io.github.derkottersberg.seamlessdogs.fabric.gametest;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/** Test-only replacement avoids nondeterministic development resource-pack precedence. */
public final class DeterministicLootFixture implements ModInitializer {
    @Override public void onInitialize() {
        LootTableEvents.REPLACE.register((resources, manager, id, original, source) -> {
            if (!id.equals(SeamlessDogs.id("digging/finds"))) return null;
            return LootTable.lootTable().setParamSet(LootContextParamSets.GIFT)
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.BONE))).build();
        });
    }
}
