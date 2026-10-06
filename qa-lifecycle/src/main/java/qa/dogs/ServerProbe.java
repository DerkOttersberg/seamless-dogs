package qa.dogs;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;

/** Creates a private, deterministic fixture, never touches the owner's worlds. */
public final class ServerProbe {
    private Wolf dog;
    private java.util.UUID positionedObserver;
    private boolean inspectedCommands;
    public void tick(MinecraftServer server) {
            if (!inspectedCommands) {
                inspectedCommands = true;
                var root = server.getCommands().getDispatcher().getRoot().getChild("dogsqa");
                System.out.println("DOGS_QA_COMMAND_TREE " + (root == null ? "absent" : root.getChildren()));
                // The installed Forge 52 test runtime can miss this test mod's
                // command-registration callback. Ensure disposable fixture
                // commands exist on the actual server before driving actions.
                if (root == null) {
                    registerCommands(server.getCommands().getDispatcher());
                    for (var player : server.getPlayerList().getPlayers()) server.getCommands().sendCommands(player);
                    System.out.println("DOGS_QA_COMMANDS_INSTALLED_ON_SERVER");
                }
            }
            var observer = server.getPlayerList().getPlayers().stream().filter(p -> p.getName().getString().equals("DogObserver")).findFirst().orElse(null);
            if (observer != null && !observer.getUUID().equals(positionedObserver)) {
                observer.setGameMode(GameType.CREATIVE);
                observer.connection.teleport(-2.5, 65, 1.4, -90, 20);
                positionedObserver = observer.getUUID();
            }
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
            dog = new Wolf(EntityType.WOLF, level);
            dog.setPos(0.5, 65, 2.0);
            dog.tame(player); dog.setOrderedToSit(true); dog.setInSittingPose(true); dog.setNoAi(true);
            dog.setYRot(180); dog.yBodyRot = 180; dog.yHeadRot = 180;
            level.addFreshEntity(dog);
            System.out.println("DOGS_QA_FIXTURE player=" + player.getUUID() + " dog=" + dog.getId());
    }
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dogsqa").then(Commands.literal("cat_idle").executes(context->{
            var owner=context.getSource().getPlayerOrException();var level=owner.level();
            for(var existing:level.getEntitiesOfClass(net.minecraft.world.entity.animal.Cat.class,owner.getBoundingBox().inflate(8)))if(existing.isOwnedBy(owner))existing.discard();
            var pet=new net.minecraft.world.entity.animal.Cat(EntityType.CAT,level);pet.tame(owner);pet.setPos(.5,65,2);pet.setNoAi(true);pet.setOnGround(true);level.addFreshEntity(pet);
            qa.dogs.mixin.IdleStateAccess.qa$start(owner,pet,io.github.derkottersberg.seamlessdogs.gameplay.PetAction.GROOM,null,2);
            return 1;
        })));
        dispatcher.register(
            Commands.literal("dogsqa")
                .then(Commands.literal("dimension").executes(context -> {
                    var server = context.getSource().getServer();
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "execute in minecraft:the_nether run tp DogQA 0.5 90 0.5"); return 1;
                }))
                .then(Commands.literal("home").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    if (!qa.dogs.mixin.IdleStateAccess.qa$sessions().isEmpty()) throw new IllegalStateException("Server retained a lifecycle session");
                    var server = context.getSource().getServer();
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "execute in minecraft:overworld run tp DogQA 0.5 65 0.5");
                    player.setGameMode(GameType.CREATIVE); return 1;
                }))
                .then(Commands.literal("die").executes(context -> {
                    var server = context.getSource().getServer();
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "kill DogQA"); return 1;
                }))
                .then(Commands.literal("observer_far").executes(context -> {
                    var observer = context.getSource().getServer().getPlayerList().getPlayers().stream().filter(p -> p.getName().getString().equals("DogObserver")).findFirst().orElseThrow();
                    observer.connection.teleport(100, 65, 100, 0, 0); return 1;
                }))
                .then(Commands.literal("observer_near").executes(context -> {
                    var observer = context.getSource().getServer().getPlayerList().getPlayers().stream().filter(p -> p.getName().getString().equals("DogObserver")).findFirst().orElseThrow();
                    observer.connection.teleport(-2.5, 65, 1.4, -90, 20); return 1;
                }))
                .then(Commands.literal("held").executes(context -> { System.out.println("DOGS_QA_HELD_SOURCE " + context.getSource().getEntity()); context.getSource().getPlayerOrException().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK)); return 1; }))
                .then(Commands.literal("puppy").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    for (var entity : player.level().getEntitiesOfClass(Wolf.class, player.getBoundingBox().inflate(5)))
                        if (entity.isOwnedBy(player)) entity.setAge(-24000);
                    return 1;
                }))
                .then(Commands.literal("empty").executes(context -> { context.getSource().getPlayerOrException().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); return 1; })));
    }
}
