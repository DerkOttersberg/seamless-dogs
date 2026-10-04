package qa.dogs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;

/** Creates a private, deterministic fixture, never touches the owner's worlds. */
public final class ServerProbe implements ModInitializer {
    private Wolf dog;
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var player = server.getPlayerList().getPlayers().stream().filter(p -> p.getName().getString().equals("DogQA")).findFirst().orElse(null);
            if (player == null || dog != null && !dog.isRemoved()) return;
            var level = player.level();
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set 6000");
            for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++) {
                level.setBlockAndUpdate(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState());
                for (int y = 65; y <= 72; y++) level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
            }
            player.setGameMode(GameType.CREATIVE);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.connection.teleport(0.5, 65, 0.5, 0, 32);
            dog = new Wolf(EntityTypes.WOLF, level);
            dog.setComponent(net.minecraft.core.component.DataComponents.WOLF_VARIANT,
                level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.WOLF_VARIANT)
                    .getOrThrow(net.minecraft.world.entity.animal.wolf.WolfVariants.PALE));
            dog.setPos(0.5, 65, 2.0);
            dog.tame(player); dog.setOrderedToSit(true); dog.setInSittingPose(true); dog.setNoAi(true);
            dog.setYRot(180); dog.yBodyRot = 180; dog.yHeadRot = 180;
            level.addFreshEntity(dog);
            System.out.println("DOGS_QA_FIXTURE player=" + player.getUUID() + " dog=" + dog.getId());
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> dispatcher.register(
            Commands.literal("dogsqa")
                .then(Commands.literal("held").executes(context -> { context.getSource().getPlayerOrException().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK)); return 1; }))
                .then(Commands.literal("empty").executes(context -> { context.getSource().getPlayerOrException().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); return 1; }))));
    }
}
