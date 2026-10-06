package qa.dogs;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
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
public final class ServerProbe {
    private Wolf dog;
    private java.util.UUID positionedObserver;
    public void tick(MinecraftServer server) {
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
                level.setBlockAndUpdate(new BlockPos(x,63,z),Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState());
                for (int y = 65; y <= 72; y++) level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
            }
            player.setGameMode(GameType.CREATIVE);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.connection.teleport(0.5, 65, 0.5, 0, 32);
            for(var existing:level.getEntitiesOfClass(net.minecraft.world.entity.TamableAnimal.class,new net.minecraft.world.phys.AABB(-8,60,-8,8,74,8)))existing.discard();
            dog = new Wolf(EntityTypes.WOLF, level);
            dog.setPos(0.5, 65, 2.0);
            dog.tame(player); dog.setOrderedToSit(true); dog.setInSittingPose(true); dog.setNoAi(true);
            dog.setYRot(180); dog.yBodyRot = 180; dog.yHeadRot = 180;
            level.addFreshEntity(dog);
            System.out.println("DOGS_QA_FIXTURE player=" + player.getUUID() + " dog=" + dog.getId());
    }
    private static void prepareDig(CommandSourceStack source,boolean cooldown) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player=source.getPlayerOrException();var state=qa.dogs.mixin.WorldStateAccess.qa$world();
        for(var cat:player.level().getEntitiesOfClass(net.minecraft.world.entity.animal.feline.Cat.class,player.getBoundingBox().inflate(8)))if(cat.isOwnedBy(player))cat.setPos(-3,65,-3);
        long now=source.getServer().overworld().getGameTime();state.dug(player.getUUID(),cooldown?now:now-12000);
        player.level().setBlockAndUpdate(new BlockPos(0,64,1),Blocks.GRASS_BLOCK.defaultBlockState());
        for(var wolf:player.level().getEntitiesOfClass(Wolf.class,player.getBoundingBox().inflate(8)))if(wolf.isOwnedBy(player)) {
            wolf.setAge(0);wolf.setPos(.5,65,2);wolf.setYRot(180);wolf.yBodyRot=180;wolf.setOrderedToSit(false);wolf.setInSittingPose(false);wolf.setOnGround(true);state.schedule(wolf.getUUID(),0);
        }
    }
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dogsqa").then(Commands.literal("protection_contract").executes(context->{ProtectionProbe.run(context.getSource().getPlayerOrException());return 1;})));
        for(var action:new io.github.derkottersberg.seamlessdogs.gameplay.PetAction[]{io.github.derkottersberg.seamlessdogs.gameplay.PetAction.KNEAD,io.github.derkottersberg.seamlessdogs.gameplay.PetAction.GROOM,io.github.derkottersberg.seamlessdogs.gameplay.PetAction.HEAD_TILT,io.github.derkottersberg.seamlessdogs.gameplay.PetAction.STRETCH}) {
            String command=switch(action){case HEAD_TILT->"tilt";case STRETCH->"stretch_now";default->action.name().toLowerCase(java.util.Locale.ROOT);};
            dispatcher.register(Commands.literal("dogsqa").then(Commands.literal(command).executes(context->{
                var player=context.getSource().getPlayerOrException();
                if(action==io.github.derkottersberg.seamlessdogs.gameplay.PetAction.HEAD_TILT)for(var cat:player.level().getEntitiesOfClass(net.minecraft.world.entity.animal.feline.Cat.class,player.getBoundingBox().inflate(8)))if(cat.isOwnedBy(player))cat.setPos(-3,65,-3);
                for(var pet:player.level().getEntitiesOfClass(net.minecraft.world.entity.TamableAnimal.class,player.getBoundingBox().inflate(8))) {
                    boolean matches=action==io.github.derkottersberg.seamlessdogs.gameplay.PetAction.HEAD_TILT?pet instanceof Wolf:pet instanceof net.minecraft.world.entity.animal.feline.Cat;
                    if(matches&&pet.isOwnedBy(player)&&!io.github.derkottersberg.seamlessdogs.SeamlessDogs.actionActive(pet,false)) {
                        pet.setPos(.5,65,2);pet.setOrderedToSit(action==io.github.derkottersberg.seamlessdogs.gameplay.PetAction.HEAD_TILT);pet.setInSittingPose(pet.isOrderedToSit());pet.setOnGround(true);
                        float yaw=action==io.github.derkottersberg.seamlessdogs.gameplay.PetAction.HEAD_TILT?(float)Math.toDegrees(Math.atan2(-(player.getX()-pet.getX()),player.getZ()-pet.getZ())):180;
                        pet.setYRot(yaw);pet.yBodyRot=yaw;pet.yHeadRot=yaw;
                        qa.dogs.mixin.WorldStateAccess.qa$startVariant(player,pet,action,null,action==io.github.derkottersberg.seamlessdogs.gameplay.PetAction.GROOM?2:0);
                        if(action==io.github.derkottersberg.seamlessdogs.gameplay.PetAction.HEAD_TILT)qa.dogs.mixin.WorldStateAccess.qa$world().scheduleReaction(pet.getUUID(),context.getSource().getServer().overworld().getGameTime()+2400);
                    }
                }return 1;
            })));
        }
        for(int v=2;v<=2;v++) {
            final int variant=v;
            dispatcher.register(Commands.literal("dogsqa").then(Commands.literal("groom_chest").executes(context->{
                var p=context.getSource().getPlayerOrException();
                for(var cat:p.level().getEntitiesOfClass(net.minecraft.world.entity.animal.feline.Cat.class,p.getBoundingBox().inflate(8)))if(cat.isOwnedBy(p)&&!io.github.derkottersberg.seamlessdogs.SeamlessDogs.actionActive(cat,false)) {
                    cat.setPos(.5,65,2);cat.setOrderedToSit(false);cat.setInSittingPose(false);cat.setOnGround(true);cat.setYRot(180);cat.yBodyRot=180;cat.yHeadRot=180;
                    qa.dogs.mixin.WorldStateAccess.qa$startVariant(p,cat,io.github.derkottersberg.seamlessdogs.gameplay.PetAction.GROOM,null,variant);
                }return 1;
            })));
        }
        dispatcher.register(
            Commands.literal("dogsqa")
                .then(Commands.literal("rewind_client_time").executes(context->{
                    var p=context.getSource().getPlayerOrException();
                    p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTimePacket(p.level().getGameTime()-6,java.util.Map.of()));
                    return 1;
                }))
                .then(Commands.literal("pet_now").executes(context->{
                    var p=context.getSource().getPlayerOrException();
                    for(var wolf:p.level().getEntitiesOfClass(Wolf.class,p.getBoundingBox().inflate(8)))if(wolf.isOwnedBy(p)&&!io.github.derkottersberg.seamlessdogs.SeamlessDogs.actionActive(wolf,true)) {
                        wolf.hurtTime=0;qa.dogs.mixin.WorldStateAccess.qa$startVariant(p,wolf,io.github.derkottersberg.seamlessdogs.gameplay.PetAction.DOG_PET,null,0);
                    }return 1;
                }))
                .then(Commands.literal("interrupt_pet").executes(context->{
                    var p=context.getSource().getPlayerOrException();
                    for(var wolf:p.level().getEntitiesOfClass(Wolf.class,p.getBoundingBox().inflate(8)))if(wolf.isOwnedBy(p))wolf.hurtTime=10;
                    return 1;
                }))
                .then(Commands.literal("observer_far").executes(context -> {
                    var observer = context.getSource().getServer().getPlayerList().getPlayers().stream().filter(p -> p.getName().getString().equals("DogObserver")).findFirst().orElseThrow();
                    observer.connection.teleport(100, 65, 100, 0, 0); return 1;
                }))
                .then(Commands.literal("observer_near").executes(context -> {
                    var observer = context.getSource().getServer().getPlayerList().getPlayers().stream().filter(p -> p.getName().getString().equals("DogObserver")).findFirst().orElseThrow();
                    observer.connection.teleport(-2.5, 65, 1.4, -90, 20); return 1;
                }))
                .then(Commands.literal("cat").executes(context->{
                    var player=context.getSource().getPlayerOrException();var level=player.level();
                    for(var wolf:level.getEntitiesOfClass(Wolf.class,player.getBoundingBox().inflate(6)))if(wolf.isOwnedBy(player))wolf.setPos(-3,65,3);
                    var cat=new net.minecraft.world.entity.animal.feline.Cat(EntityTypes.CAT,level);cat.tame(player);cat.setPos(.5,65,2);cat.setNoAi(true);cat.setOrderedToSit(false);cat.setInSittingPose(false);cat.setYRot(180);cat.yBodyRot=180;cat.yHeadRot=180;level.addFreshEntity(cat);return 1;
                }))
                .then(Commands.literal("kitten").executes(context->{var player=context.getSource().getPlayerOrException();for(var cat:player.level().getEntitiesOfClass(net.minecraft.world.entity.animal.feline.Cat.class,player.getBoundingBox().inflate(6)))if(cat.isOwnedBy(player)){cat.setAge(-24000);cat.setOrderedToSit(true);cat.setInSittingPose(true);}return 1;}))
                .then(Commands.literal("adult").executes(context->{var player=context.getSource().getPlayerOrException();for(var cat:player.level().getEntitiesOfClass(net.minecraft.world.entity.animal.feline.Cat.class,player.getBoundingBox().inflate(6)))if(cat.isOwnedBy(player)){cat.setAge(0);cat.setOrderedToSit(false);cat.setInSittingPose(false);}return 1;}))
                .then(Commands.literal("view_front").executes(context->{context.getSource().getPlayerOrException().connection.teleport(.5,65,-.3,0,25);return 1;}))
                .then(Commands.literal("view_right").executes(context->{context.getSource().getPlayerOrException().connection.teleport(2.8,65,.5,0,25);return 1;}))
                .then(Commands.literal("view_side").executes(context->{context.getSource().getPlayerOrException().connection.teleport(-1.8,65,.5,0,25);return 1;}))
                .then(Commands.literal("stretch").executes(context->{var player=context.getSource().getPlayerOrException();for(var cat:player.level().getEntitiesOfClass(net.minecraft.world.entity.animal.feline.Cat.class,player.getBoundingBox().inflate(6)))if(cat.isOwnedBy(player)){cat.setOrderedToSit(false);cat.setInSittingPose(false);cat.setOnGround(true);qa.dogs.mixin.WorldStateAccess.qa$world().schedule(cat.getUUID(),0);}return 1;}))
                .then(Commands.literal("dig").executes(context->{prepareDig(context.getSource(),false);return 1;}))
                .then(Commands.literal("dig_sand").executes(context->{prepareDig(context.getSource(),false);context.getSource().getLevel().setBlockAndUpdate(new BlockPos(0,64,1),Blocks.SAND.defaultBlockState());return 1;}))
                .then(Commands.literal("dig_cooldown").executes(context->{prepareDig(context.getSource(),true);return 1;}))
                .then(Commands.literal("held").executes(context -> { context.getSource().getPlayerOrException().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK)); return 1; }))
                .then(Commands.literal("puppy").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    for (var entity : player.level().getEntitiesOfClass(Wolf.class, player.getBoundingBox().inflate(5)))
                        if (entity.isOwnedBy(player)) entity.setAge(-24000);
                    return 1;
                }))
                .then(Commands.literal("dog_adult").executes(context->{var p=context.getSource().getPlayerOrException();for(var wolf:p.level().getEntitiesOfClass(Wolf.class,p.getBoundingBox().inflate(8)))if(wolf.isOwnedBy(p))wolf.setAge(0);return 1;}))
                .then(Commands.literal("tilt_due").executes(context->{
                    var p=context.getSource().getPlayerOrException();
                    for(var cat:p.level().getEntitiesOfClass(net.minecraft.world.entity.animal.feline.Cat.class,p.getBoundingBox().inflate(8)))if(cat.isOwnedBy(p))cat.setPos(-3,65,-3);
                    for(var wolf:p.level().getEntitiesOfClass(Wolf.class,p.getBoundingBox().inflate(8)))if(wolf.isOwnedBy(p)) {
                        wolf.setPos(.5,65,2);wolf.setOrderedToSit(true);wolf.setInSittingPose(true);wolf.setOnGround(true);
                        float yaw=(float)Math.toDegrees(Math.atan2(-(p.getX()-wolf.getX()),p.getZ()-wolf.getZ()));wolf.setYRot(yaw);wolf.yBodyRot=yaw;wolf.yHeadRot=yaw;
                        qa.dogs.mixin.WorldStateAccess.qa$world().scheduleReaction(wolf.getUUID(),0);
                    }return 1;
                }))
                .then(Commands.literal("empty").executes(context -> { context.getSource().getPlayerOrException().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); return 1; })));
    }
}
