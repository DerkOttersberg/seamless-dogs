package io.github.derkottersberg.seamlessdogs.fabric.gametest;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.network.*;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import java.util.UUID;

@SuppressWarnings("removal")
public final class DogsGameTests {
    private record Fixture(ServerPlayer owner, Wolf dog) { }
    private Fixture fixture(GameTestHelper h) {
        ServerPlayer owner = h.makeMockServerPlayerInLevel();
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        owner.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        Wolf dog = new Wolf(EntityTypes.WOLF, h.getLevel());
        dog.setPos(owner.getX(), owner.getY(), owner.getZ() + 1.5);
        dog.tame(owner);
        dog.setOrderedToSit(true);
        h.getLevel().addFreshEntity(dog);
        return new Fixture(owner, dog);
    }
    @GameTest(maxTicks = 80)
    public void ownerCanPet(GameTestHelper h) {
        var f = fixture(h);
        float health = f.dog.getHealth();
        h.assertTrue(SeamlessDogs.request(f.owner, new PetRequest(f.dog.getId())), "Owner request rejected");
        h.assertTrue(f.dog.isOrderedToSit(), "Petting changed sit command");
        h.assertTrue(SeamlessDogs.isPetting(f.owner.getUUID()), "No authoritative session");
        h.runAfterDelay(42, () -> {
            h.assertTrue(!SeamlessDogs.isPetting(f.owner.getUUID()), "Session did not finish");
            h.assertTrue(f.dog.isOrderedToSit(), "Session changed sit command");
            h.assertValueEqual(f.dog.getHealth(), health, "MVP must not heal or harm the dog");
            System.out.println("DOGS_GAMETEST_PASS ownerCanPet");
            h.succeed();
        });
    }
    @GameTest(maxTicks = 20)
    public void rejectInvalidRequests(GameTestHelper h) {
        var f = fixture(h);
        h.assertTrue(!SeamlessDogs.request(f.owner, new PetRequest(-1)), "Negative target accepted");
        h.assertTrue(!SeamlessDogs.request(f.owner, new PetRequest(Integer.MAX_VALUE)), "Unknown target accepted");
        f.dog.setOwnerReference(net.minecraft.world.entity.EntityReference.of(UUID.randomUUID()));
        h.assertTrue(!SeamlessDogs.request(f.owner, new PetRequest(f.dog.getId())), "Other owner's dog accepted");
        f.dog.tame(f.owner);
        f.owner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        h.assertTrue(!SeamlessDogs.request(f.owner, new PetRequest(f.dog.getId())), "Occupied hand accepted");
        f.owner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        f.dog.setPos(f.owner.getX() + 6, f.owner.getY(), f.owner.getZ());
        h.assertTrue(!SeamlessDogs.request(f.owner, new PetRequest(f.dog.getId())), "Distant dog accepted");
        f.dog.setPos(f.owner.getX(), f.owner.getY(), f.owner.getZ() + 2);
        BlockPos wall = BlockPos.containing(f.owner.getX(), f.owner.getY(), f.owner.getZ() + 1);
        h.getLevel().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(wall.above(), Blocks.STONE.defaultBlockState());
        h.assertTrue(!SeamlessDogs.request(f.owner, new PetRequest(f.dog.getId())), "Petting through wall accepted");
        System.out.println("DOGS_GAMETEST_PASS rejectInvalidRequests");
        h.succeed();
    }
    @GameTest(maxTicks = 30)
    public void cooldownAndCancellation(GameTestHelper h) {
        var f = fixture(h);
        h.assertTrue(SeamlessDogs.request(f.owner, new PetRequest(f.dog.getId())), "Initial request rejected");
        h.assertTrue(!SeamlessDogs.request(f.owner, new PetRequest(f.dog.getId())), "Duplicate request restarted animation/sound");
        f.owner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        h.runAfterDelay(3, () -> {
            h.assertTrue(!SeamlessDogs.isPetting(f.owner.getUUID()), "Changed hand did not cancel");
            f.owner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            h.assertTrue(!SeamlessDogs.request(f.owner, new PetRequest(f.dog.getId())), "Cooldown bypassed after cancellation");
            SeamlessDogs.disconnect(f.owner);
            h.assertTrue(!SeamlessDogs.isPetting(f.owner.getUUID()), "Disconnect retained session");
            System.out.println("DOGS_GAMETEST_PASS cooldownAndCancellation");
            h.succeed();
        });
    }
    @GameTest(maxTicks = 10)
    public void codecRoundTrip(GameTestHelper h) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
        try {
            var request = new PetRequest(27); PetRequest.CODEC.encode(buffer, request);
            h.assertValueEqual(PetRequest.CODEC.decode(buffer), request, "Request codec");
            var state = new PetState(UUID.randomUUID(), UUID.randomUUID(), 40); PetState.CODEC.encode(buffer, state);
            h.assertValueEqual(PetState.CODEC.decode(buffer), state, "State codec");
        } finally { buffer.release(); }
        System.out.println("DOGS_GAMETEST_PASS codecRoundTrip");
        h.succeed();
    }
}
