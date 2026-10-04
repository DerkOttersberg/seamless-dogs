package io.github.derkottersberg.seamlessdogs.gametest;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.network.*;
import io.netty.buffer.Unpooled;
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
public final class DogsScenarios {
    private record Fixture(ServerPlayer owner, Wolf dog) { }
    private static Fixture fixture(GameTestHelper h) {
        ServerPlayer owner = h.makeMockServerPlayerInLevel();
        for (int x = 0; x < 3; x++) for (int z = 0; z < 4; z++)
            h.setBlock(new BlockPos(x, 0, z), Blocks.STONE.defaultBlockState());
        var pos = h.absolutePos(new BlockPos(1, 1, 1));
        owner.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        owner.setNoGravity(true);
        Wolf dog = new Wolf(EntityTypes.WOLF, h.getLevel());
        dog.setPos(owner.getX(), owner.getY(), owner.getZ() + 1);
        dog.setNoGravity(true);
        dog.tame(owner);
        dog.setOrderedToSit(true);
        dog.setNoAi(true);
        h.getLevel().addFreshEntity(dog);
        h.assertTrue(SeamlessDogs.canPet(owner, dog), "Invalid QA fixture: alive=" + owner.isAlive()
            + " spectator=" + owner.isSpectator() + " main=" + owner.getMainHandItem()
            + " owned=" + dog.isOwnedBy(owner) + " angry=" + dog.isAngry()
            + " LOS=" + owner.hasLineOfSight(dog) + " distance=" + owner.distanceToSqr(dog));
        h.assertTrue(h.getLevel().getEntity(dog.getId()) == dog, "QA dog not yet in entity index");
        return new Fixture(owner, dog);
    }
    public static void ownerCanPet(GameTestHelper h) {
        var f = fixture(h);
        float health = f.dog.getHealth();
        h.assertTrue(SeamlessDogs.request(f.owner, new PetRequest(f.dog.getId())), "Owner request rejected");
        h.assertTrue(f.dog.isOrderedToSit(), "Petting changed sit command");
        h.assertTrue(SeamlessDogs.isPetting(f.owner.getUUID()), "No authoritative session");
        h.runAfterDelay(42, () -> {
            h.assertTrue(!SeamlessDogs.isPetting(f.owner.getUUID()), "Session did not finish");
            h.assertTrue(f.dog.isOrderedToSit(), "Session changed sit command; health=" + f.dog.getHealth()
                + " initial=" + health + " alive=" + f.dog.isAlive() + " position=" + f.dog.position());
            h.assertValueEqual(f.dog.getHealth(), health, "MVP must not heal or harm the dog");
            org.slf4j.LoggerFactory.getLogger("SeamlessDogsTests").info("DOGS_GAMETEST_PASS ownerCanPet");
            h.succeed();
        });
    }
    public static void rejectInvalidRequests(GameTestHelper h) {
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
        org.slf4j.LoggerFactory.getLogger("SeamlessDogsTests").info("DOGS_GAMETEST_PASS rejectInvalidRequests");
        h.succeed();
    }
    public static void cooldownAndCancellation(GameTestHelper h) {
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
            org.slf4j.LoggerFactory.getLogger("SeamlessDogsTests").info("DOGS_GAMETEST_PASS cooldownAndCancellation");
            h.succeed();
        });
    }
    public static void codecRoundTrip(GameTestHelper h) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
        try {
            var request = new PetRequest(27); PetRequest.CODEC.encode(buffer, request);
            h.assertValueEqual(PetRequest.CODEC.decode(buffer), request, "Request codec");
            var state = new PetState(UUID.randomUUID(), UUID.randomUUID(), 40); PetState.CODEC.encode(buffer, state);
            h.assertValueEqual(PetState.CODEC.decode(buffer), state, "State codec");
        } finally { buffer.release(); }
        org.slf4j.LoggerFactory.getLogger("SeamlessDogsTests").info("DOGS_GAMETEST_PASS codecRoundTrip");
        h.succeed();
    }
}
