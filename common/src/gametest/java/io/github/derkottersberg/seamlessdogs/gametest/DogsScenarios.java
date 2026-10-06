package io.github.derkottersberg.seamlessdogs.gametest;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.network.*;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import java.util.UUID;

@SuppressWarnings("removal")
public final class DogsScenarios {
    private static java.util.function.Function<GameTestHelper,ServerPlayer> playerFactory=GameTestHelper::makeMockServerPlayerInLevel;
    public static void usePlayerFactory(java.util.function.Function<GameTestHelper,ServerPlayer> factory){playerFactory=factory;}
    private record Fixture(ServerPlayer owner, Wolf dog) { }
    private static Fixture fixture(GameTestHelper h) {
        ServerPlayer owner = playerFactory.apply(h);
        for (int x = 0; x < 3; x++) for (int z = 0; z < 4; z++) {
            h.setBlock(new BlockPos(x, 0, z), Blocks.STONE.defaultBlockState());
            // NeoForge's empty template encloses the fixture in barriers.
            for(int y=1;y<=4;y++)h.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
        }
        var pos = h.absolutePos(new BlockPos(1, 1, 1));
        // Test placement is randomized and can straddle an initially unloaded chunk.
        for (int x=-1;x<=1;x++) for(int z=-1;z<=1;z++)
            {h.getLevel().setChunkForced((pos.getX()>>4)+x,(pos.getZ()>>4)+z,true);h.getLevel().getChunkAt(pos.offset(x*16,0,z*16));}
        owner.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        owner.setNoGravity(true);
        Wolf dog = new Wolf(EntityType.WOLF, h.getLevel());
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
        f.dog.setHealth(Math.max(1.0F, f.dog.getMaxHealth() - 7.0F));
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
        f.dog.setOwnerUUID(UUID.randomUUID());
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
    public static void catOwnership(GameTestHelper h) {
        var f=fixture(h);
        var cat=new net.minecraft.world.entity.animal.Cat(EntityType.CAT,h.getLevel());
        cat.setPos(f.owner.getX(),f.owner.getY(),f.owner.getZ()+1);cat.tame(f.owner);cat.setNoAi(true);cat.setNoGravity(true);h.getLevel().addFreshEntity(cat);
        h.assertTrue(SeamlessDogs.canPet(f.owner,cat),"Owned cat cannot be petted: owned="+cat.isOwnedBy(f.owner)+" target="+cat.getTarget()+" hurt="+cat.hurtTime+" lying="+cat.isLying()+" LOS="+f.owner.hasLineOfSight(cat)+" alive="+cat.isAlive()+" distance="+f.owner.distanceToSqr(cat));
        cat.setAge(-24000);h.assertTrue(SeamlessDogs.canPet(f.owner,cat),"Kitten cannot be petted");
        cat.setOwnerUUID(UUID.randomUUID());
        h.assertTrue(!SeamlessDogs.canPet(f.owner,cat),"Foreign cat accepted");
        cat.tame(f.owner);cat.setLying(true);h.assertTrue(!SeamlessDogs.canPet(f.owner,cat),"Sleeping cat disturbed");
        org.slf4j.LoggerFactory.getLogger("SeamlessDogsTests").info("DOGS_GAMETEST_PASS catOwnership");h.succeed();
    }
    public static void diggingTerrain(GameTestHelper h) {
        var f=fixture(h);var pos=h.absolutePos(new BlockPos(1,0,1));
        h.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(pos,Blocks.DIRT.defaultBlockState());
        h.assertTrue(io.github.derkottersberg.seamlessdogs.gameplay.DigSite.eligible(h.getLevel(),pos),"Exposed dirt rejected: tagged="+h.getLevel().getBlockState(pos).is(io.github.derkottersberg.seamlessdogs.gameplay.DigSite.DIGGABLE)+" above="+h.getLevel().getBlockState(pos.above())+" below="+h.getLevel().getBlockState(pos.below())+" border="+h.getLevel().getWorldBorder().isWithinBounds(pos));
        h.getLevel().setBlockAndUpdate(pos.above(),Blocks.OAK_SAPLING.defaultBlockState());
        h.assertTrue(!io.github.derkottersberg.seamlessdogs.gameplay.DigSite.eligible(h.getLevel(),pos),"Plant support can be destroyed");
        h.getLevel().setBlockAndUpdate(pos.above(),Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(pos,Blocks.FARMLAND.defaultBlockState());
        h.assertTrue(!io.github.derkottersberg.seamlessdogs.gameplay.DigSite.eligible(h.getLevel(),pos),"Farmland accepted");
        h.getLevel().setBlockAndUpdate(pos,Blocks.DIRT.defaultBlockState());h.getLevel().setBlockAndUpdate(pos.east(),Blocks.WATER.defaultBlockState());
        h.assertTrue(!io.github.derkottersberg.seamlessdogs.gameplay.DigSite.eligible(h.getLevel(),pos),"Fluid edge accepted");
        org.slf4j.LoggerFactory.getLogger("SeamlessDogsTests").info("DOGS_GAMETEST_PASS diggingTerrain");h.succeed();
    }
    public static void v2CodecRoundTrip(GameTestHelper h) {
        var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),h.getLevel().registryAccess());
        try {
            var control=new PetControl(2,7,42);PetControl.CODEC.encode(buffer,control);h.assertValueEqual(PetControl.CODEC.decode(buffer),control,"Control codec");
            var update=new PetUpdate(UUID.randomUUID(),UUID.randomUUID(),4,37,123,0,0,"");PetUpdate.CODEC.encode(buffer,update);h.assertValueEqual(PetUpdate.CODEC.decode(buffer),update,"Action codec");
            for(int variant=0;variant<3;variant++){var groom=new PetUpdate(UUID.randomUUID(),UUID.randomUUID(),7,46,124,variant,0,"");PetUpdate.CODEC.encode(buffer,groom);h.assertValueEqual(PetUpdate.CODEC.decode(buffer),groom,"Groom variant codec");}
            var settings=new PetUpdate(UUID.randomUUID(),PetUpdate.NONE,5,0,0,63,123,"Claimed land is excluded");PetUpdate.CODEC.encode(buffer,settings);h.assertValueEqual(PetUpdate.CODEC.decode(buffer),settings,"Settings codec");
            for(var action:io.github.derkottersberg.seamlessdogs.gameplay.PetAction.values()){
                h.assertTrue(action.wireId!=5,"Animation collides with settings");
                var clip=new PetUpdate(UUID.randomUUID(),UUID.randomUUID(),action.wireId,action.duration-1,25,0,0,"");PetUpdate.CODEC.encode(buffer,clip);h.assertValueEqual(PetUpdate.CODEC.decode(buffer),clip,"Expressive clip codec");
            }
        } finally {buffer.release();}
        org.slf4j.LoggerFactory.getLogger("SeamlessDogsTests").info("DOGS_GAMETEST_PASS v2CodecRoundTrip");h.succeed();
    }

    /** Native world ticks with an explicit protection adapter and deterministic test-only loot table. */
    public static void idleContracts(GameTestHelper h) {
        var f=fixture(h);var server=h.getLevel().getServer();
        var original=privateState("platform",io.github.derkottersberg.seamlessdogs.internal.PlatformServices.class);
        var allow=new java.util.concurrent.atomic.AtomicBoolean(false);
        var snapshot=new java.util.concurrent.atomic.AtomicReference<PetUpdate>();
        SeamlessDogs.initialize(new io.github.derkottersberg.seamlessdogs.internal.PlatformServices() {
            public void sendToPlayer(ServerPlayer p,net.minecraft.network.protocol.common.custom.CustomPacketPayload packet){if(packet instanceof PetUpdate update)snapshot.set(update);}
            public void sendToTrackingAndSelf(ServerPlayer p,net.minecraft.world.entity.Entity pet,net.minecraft.network.protocol.common.custom.CustomPacketPayload packet){}
            public boolean supportsV2(ServerPlayer p){return true;}
            public boolean mayDig(ServerPlayer p,net.minecraft.world.entity.TamableAnimal pet,BlockPos pos,boolean commit){return allow.get()&&io.github.derkottersberg.seamlessdogs.gameplay.DigPermissions.vanilla(p,pet,pos);}
            public String protectionStatus(){return "Native contract fixture";}
        });
        SeamlessDogs.control(f.owner,new PetControl(0,0,0));
        f.dog.setOnGround(true);
        h.assertTrue(SeamlessDogs.reactionEligible(f.owner,f.dog),"Calm seated dog cannot react");
        var gaze=f.dog.getEyePosition().subtract(f.owner.getEyePosition());
        f.owner.setYRot((float)Math.toDegrees(Math.atan2(-gaze.x,gaze.z)));f.owner.setXRot((float)-Math.toDegrees(Math.atan2(gaze.y,Math.sqrt(gaze.x*gaze.x+gaze.z*gaze.z))));
        h.assertTrue(SeamlessDogs.lookingAt(f.owner,f.dog),"Looking at dog rejected");
        f.owner.setYRot(f.owner.getYRot()+90);h.assertTrue(!SeamlessDogs.lookingAt(f.owner,f.dog),"Looking away still triggers reaction");
        f.dog.setAge(-24000);h.assertTrue(SeamlessDogs.reactionEligible(f.owner,f.dog),"Calm puppy cannot react");f.dog.setAge(0);
        f.dog.hurtTime=5;h.assertTrue(!SeamlessDogs.reactionEligible(f.owner,f.dog),"Hurt dog can react");f.dog.hurtTime=0;
        h.assertTrue(!SeamlessDogs.mayReceive(f.owner,new PetUpdate(f.owner.getUUID(),f.dog.getUUID(),8,0,26,0,0,"")),"Unnegotiated client receives new clip");
        var state=privateState("world",io.github.derkottersberg.seamlessdogs.gameplay.PetWorldState.class);
        state.update(f.owner.getUUID(),false,7,state.revision());
        state.update(f.owner.getUUID(),true,1,state.revision());
        long revision=state.revision();
        h.assertTrue(snapshot.get()!=null,"No capability/settings snapshot");
        h.assertTrue((snapshot.get().flags()&16)==0,"Fixture must be a non-admin");
        var site=f.dog.blockPosition().below().south();
        for(int x=0;x<3;x++)for(int z=0;z<4;z++) {
            var pos=h.absolutePos(new BlockPos(x,0,z));
            h.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());
            h.getLevel().setBlockAndUpdate(pos,Blocks.DIRT.defaultBlockState());
        }
        h.assertTrue(!SeamlessDogs.idleEligible(f.owner,f.dog),"Sitting pet eligible");
        f.dog.setOrderedToSit(false);f.dog.setInSittingPose(false);f.dog.setOnGround(true);
        f.dog.setNoGravity(false);f.dog.setYRot(0);f.dog.yBodyRot=0;f.dog.yHeadRot=0;
        h.assertTrue(SeamlessDogs.idleEligible(f.owner,f.dog),"Calm grounded adult rejected");
        f.dog.setAge(-24000);h.assertTrue(!SeamlessDogs.idleEligible(f.owner,f.dog),"Puppy can dig");f.dog.setAge(0);
        f.dog.hurtTime=5;h.assertTrue(!SeamlessDogs.idleEligible(f.owner,f.dog),"Damaged dog eligible");f.dog.hurtTime=0;
        f.dog.setDeltaMovement(.1,0,0);h.assertTrue(!SeamlessDogs.idleEligible(f.owner,f.dog),"Moving dog eligible");f.dog.setDeltaMovement(0,0,0);
        var rules=h.getLevel().getGameRules();var grief=net.minecraft.world.level.GameRules.RULE_MOBGRIEFING;
        boolean previousGrief=rules.getBoolean(grief);rules.getRule(grief).set(false,server);
        h.assertTrue(!io.github.derkottersberg.seamlessdogs.gameplay.DigPermissions.vanilla(f.owner,f.dog,site),"mobGriefing denied terrain change was allowed");rules.getRule(grief).set(previousGrief,server);
        state.dug(f.owner.getUUID(),server.overworld().getGameTime()-12000);state.schedule(f.dog.getUUID(),0);
        h.runAfterDelay(5,()->{
            SeamlessDogs.control(f.owner,new PetControl(2,0,revision));
            h.assertTrue(state.digging()&&state.revision()==revision,"Non-admin changed world rules");
        });
        h.runAfterDelay(30,()->{
            h.assertTrue(!SeamlessDogs.actionActive(f.dog,false)&&!h.getLevel().getBlockState(site).isAir(),"Denied protection changed terrain");
            long now=server.overworld().getGameTime();
            h.assertTrue(state.due(f.dog.getUUID())>=now+11960&&state.due(f.dog.getUUID())<=now+24000,"Dig attempt interval is outside 10–20 minutes");
            allow.set(true);state.schedule(f.dog.getUUID(),0);
        });
        h.runAfterDelay(130,()->{
            h.assertTrue(h.getLevel().getBlockState(site).isAir(),"Successful dig did not remove its block");
            h.assertTrue(!state.ownerReady(f.owner.getUUID(),server.overworld().getGameTime()),"Owner limit missing");
            int bones=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,f.dog.getBoundingBox().inflate(5)).stream().filter(e->e.getItem().is(Items.BONE)).mapToInt(e->e.getItem().getCount()).sum();
            h.assertTrue(bones==1,"Bonus loot generated more than once, or missing deterministic test loot: "+bones);
            h.getLevel().setBlockAndUpdate(site,Blocks.DIRT.defaultBlockState());state.schedule(f.dog.getUUID(),0);
        });
        var idleCat=new net.minecraft.world.entity.animal.Cat(EntityType.CAT,h.getLevel());
        idleCat.tame(f.owner);idleCat.setAge(-24000);idleCat.setPos(f.owner.getX()-1,f.owner.getY(),f.owner.getZ());idleCat.setNoAi(true);
        h.getLevel().addFreshEntity(idleCat);
        h.runAfterDelay(170,()->{idleCat.setOnGround(true);state.schedule(idleCat.getUUID(),0);});
        h.runAfterDelay(200,()->{
            h.assertTrue(SeamlessDogs.actionActive(idleCat,false),"Kitten stretch did not start");
            long now=server.overworld().getGameTime();
            h.assertTrue(state.due(idleCat.getUUID())>=now+3560&&state.due(idleCat.getUUID())<=now+7200,"Cat idle interval is outside 3–6 minutes");
            h.assertTrue(SeamlessDogs.request(f.owner,new PetRequest(idleCat.getId())),"Petting did not interrupt stretch");
            h.assertTrue(!SeamlessDogs.actionActive(idleCat,false),"Stretch retained idle action after petting");idleCat.hurtTime=5;
        });
        h.runAfterDelay(205,()->{
            try {
                h.assertTrue(!SeamlessDogs.isPetting(f.owner.getUUID()),"Damage did not cancel cat petting");
                h.assertTrue(!h.getLevel().getBlockState(site).isAir(),"Owner successful-dig cooldown bypassed");
                state.save();
                state.scheduleReaction(f.dog.getUUID(),server.overworld().getGameTime()+1800);state.save();
                var restored=new io.github.derkottersberg.seamlessdogs.gameplay.PetWorldState(server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
                h.assertTrue(!restored.ownerReady(f.owner.getUUID(),server.overworld().getGameTime()),"Owner cooldown not persisted");
                h.assertValueEqual(restored.due(f.dog.getUUID()),state.due(f.dog.getUUID()),"Interrupted/attempted cooldown persistence");
                h.assertValueEqual(restored.reactionDue(f.dog.getUUID()),state.reactionDue(f.dog.getUUID()),"Gaze reaction cooldown persistence");
                SeamlessDogs.control(f.owner,new PetControl(0,1,0));
                f.dog.setNoAi(false);f.dog.setOrderedToSit(true);f.dog.setInSittingPose(true);f.dog.setOnGround(true);f.dog.setDeltaMovement(0,0,0);
                // Keep the target within the vanilla seated wolf's head-yaw limit.
                // A target directly behind it otherwise depends on randomized body AI.
                f.dog.setYRot(0);f.dog.yBodyRot=0;f.dog.yHeadRot=0;
                f.owner.setPos(f.dog.getX()-1,f.dog.getY(),f.dog.getZ()+1);
                var look=f.dog.getEyePosition().subtract(f.owner.getEyePosition());
                f.owner.setYRot((float)Math.toDegrees(Math.atan2(-look.x,look.z)));f.owner.setXRot((float)-Math.toDegrees(Math.atan2(look.y,Math.sqrt(look.x*look.x+look.z*look.z))));
                state.scheduleReaction(f.dog.getUUID(),server.overworld().getGameTime()+5);
            }catch(RuntimeException e){SeamlessDogs.initialize(original);throw e;}
        });
        // The owner keeps looking as native AI settles the sitting pose.
        for(int tick=206;tick<255;tick++)h.runAfterDelay(tick,()->{
            var look=f.dog.getEyePosition().subtract(f.owner.getEyePosition());
            f.owner.setYRot((float)Math.toDegrees(Math.atan2(-look.x,look.z)));
            f.owner.setXRot((float)-Math.toDegrees(Math.atan2(look.y,Math.sqrt(look.x*look.x+look.z*look.z))));
        });
        h.runAfterDelay(255,()->{
            h.assertTrue(SeamlessDogs.reactionActive(f.dog),"Gaze did not start a seated dog's native AI reaction: eligible="+SeamlessDogs.reactionEligible(f.owner,f.dog)+" looking="+SeamlessDogs.lookingAt(f.owner,f.dog)+" ground="+f.dog.onGround()+" motion="+f.dog.getDeltaMovement()+" owner="+f.owner.position()+" dog="+f.dog.position()+" due="+state.reactionDue(f.dog.getUUID())+" now="+server.overworld().getGameTime());
            h.assertTrue(f.dog.isOrderedToSit()&&f.dog.getNavigation().isDone(),"Reaction changed the sit command or navigation");
            var look=f.owner.position().subtract(f.dog.position());float yaw=(float)Math.toDegrees(Math.atan2(-look.x,look.z));
            h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(f.dog.yHeadRot-yaw))<20,"Seated dog failed to look back: head="+f.dog.yHeadRot+" body="+f.dog.yBodyRot+" expected="+yaw+" gameTick="+server.overworld().getGameTime());
            f.dog.hurtTime=5;
        });
        h.runAfterDelay(258,()->{
            try {
                h.assertTrue(!SeamlessDogs.reactionActive(f.dog),"Damage did not cancel gaze reaction");
                h.assertTrue(state.reactionDue(f.dog.getUUID())>server.overworld().getGameTime()+1100,"Interrupted reaction lost its cooldown");
                org.slf4j.LoggerFactory.getLogger("SeamlessDogsTests").info("DOGS_GAMETEST_PASS idleContracts");h.succeed();
            }finally{SeamlessDogs.initialize(original);}
        });
    }
    private static <T> T privateState(String name,Class<T> type) {
        try {var field=SeamlessDogs.class.getDeclaredField(name);field.setAccessible(true);return type.cast(field.get(null));}
        catch(ReflectiveOperationException exception){throw new IllegalStateException(exception);}
    }

}
