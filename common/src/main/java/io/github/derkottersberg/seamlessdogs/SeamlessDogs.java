package io.github.derkottersberg.seamlessdogs;

import io.github.derkottersberg.seamlessdogs.internal.PlatformServices;
import io.github.derkottersberg.seamlessdogs.network.*;
import io.github.derkottersberg.seamlessdogs.gameplay.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;

/** Authorization, random choices, world mutation and action timing belong to the server. */
public final class SeamlessDogs {
    public static final int DURATION = 40, COOLDOWN = 60;
    public static final double REACH = 3;
    private static PlatformServices platform;
    private static final Map<UUID, Session> sessions = new HashMap<>();
    private static final Map<UUID, Long> nextAllowed = new HashMap<>(), controlAllowed = new HashMap<>();
    private static final Set<UUID> modern = new HashSet<>();
    private static final Set<UUID> expressive = new HashSet<>();
    private static PetWorldState world;
    private static MinecraftServer currentServer;
    private static long sequence;
    private record RequestBudget(long tick,int count){}
    private static final Map<UUID,RequestBudget> requestBudgets=new HashMap<>();
    private SeamlessDogs() { }
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("seamlessdogs", path); }
    public static void initialize(PlatformServices services) { platform = services; }
    private static void world(MinecraftServer server) {
        if (currentServer != server) { clear(); currentServer = server; world = new PetWorldState(server.getWorldPath(LevelResource.ROOT)); }
    }
    public static boolean canPet(ServerPlayer player, TamableAnimal pet) {
        return (pet instanceof Wolf || pet instanceof Cat) && player.isAlive() && !player.isSpectator()
            && !player.isPassenger() && !player.isUsingItem() && player.getMainHandItem().isEmpty()
            && pet.isAlive() && pet.isTame() && pet.isOwnedBy(player) && pet.hurtTime == 0
            && (!(pet instanceof Wolf wolf) || !wolf.isAngry()) && pet.getTarget() == null
            && (!(pet instanceof Cat cat) || !cat.isLying()) && player.level() == pet.level()
            && player.distanceToSqr(pet) <= REACH * REACH && player.hasLineOfSight(pet);
    }
    public static boolean request(ServerPlayer player, PetRequest request) {
        world(player.level().getServer());
        long requestTick=currentServer.overworld().getGameTime();
        var budget=requestBudgets.get(player.getUUID());
        int count=budget!=null&&budget.tick()==requestTick?budget.count():0;
        if(count>=8)return false;
        requestBudgets.put(player.getUUID(),new RequestBudget(requestTick,count+1));
        if (request.dogId() < 0 || isPetting(player.getUUID()) || nextAllowed.getOrDefault(player.getUUID(),0L)>requestTick) return false;
        Entity entity = player.level().getEntity(request.dogId());
        if (!(entity instanceof TamableAnimal pet) || !canPet(player, pet)
            || (pet instanceof Cat && !modern.contains(player.getUUID()))) return false;
        long now = currentServer.overworld().getGameTime();
        if (isPetting(player.getUUID()) || nextAllowed.getOrDefault(player.getUUID(), 0L) > now) return false;
        Session previous = sessions.get(pet.getUUID());
        if (previous != null && previous.action.petting()) return false;
        if (previous != null) stop(previous);
        start(player, pet, pet instanceof Cat ? PetAction.CAT_PET : PetAction.DOG_PET, null);
        nextAllowed.put(player.getUUID(), now + COOLDOWN);
        return true;
    }
    public static void control(ServerPlayer player, PetControl request) {
        world(player.level().getServer());
        long now = currentServer.overworld().getGameTime();
        if (!platform.supportsV2(player) || request.operation() < 0 || request.operation() > 2 || request.flags() < 0 || request.flags() > 7
            || request.revision() < 0) return;
        if(controlAllowed.getOrDefault(player.getUUID(),0L)>now)return;
        controlAllowed.put(player.getUUID(), now + 4);
        modern.add(player.getUUID());
        if(request.operation()==0&&(request.flags()&1)!=0)expressive.add(player.getUUID());
        String status = platform.protectionStatus();
        if (request.operation() != 0) {
            boolean permitted = request.operation() == 1 || admin(player);
            boolean saved = permitted && world.update(player.getUUID(), request.operation() == 1, request.flags(), request.revision());
            if (!saved) status = "Settings were not saved. Refresh and try again.";
            if (saved) {
                for (ServerPlayer recipient : currentServer.getPlayerList().getPlayers())
                    if (modern.contains(recipient.getUUID())) settings(recipient, platform.protectionStatus());
                return;
            }
        }
        settings(player, status);
        if(request.operation()==0) for(Session s:sessions.values()) {
            if(s.pet.level()==player.level() && player.distanceToSqr(s.pet)<=16384)
                platform.sendToPlayer(player,update(s,(int)Math.max(0,Math.min(s.action.duration,now-s.start)),false));
        }
    }
    private static boolean admin(ServerPlayer player) {
        return currentServer.isSingleplayerOwner(player.getGameProfile()) || player.hasPermissions(2);
    }
    private static void settings(ServerPlayer player, String status) {
        if(!world.writable())status="World pet settings could not be read safely; idle behaviors disabled. Original file preserved.";
        int flags = (world.digging() ? 1 : 0) | (world.finds() ? 2 : 0) | (world.stretching() ? 4 : 0)
            | (world.ownerDigging(player.getUUID()) ? 8 : 0) | (admin(player) ? 16 : 0) | (world.writable() ? 32 : 0);
        platform.sendToPlayer(player, new PetUpdate(player.getUUID(), PetUpdate.NONE, 5, 0, 0, flags, world.revision(), status));
    }
    private static void start(ServerPlayer player, TamableAnimal pet, PetAction action, BlockPos site) {
        startVariant(player,pet,action,site,action==PetAction.GROOM?2:0);
    }
    private static void startVariant(ServerPlayer player,TamableAnimal pet,PetAction action,BlockPos site,int variant) {
        if(variant<0||variant>2||(action!=PetAction.GROOM&&variant!=0))throw new IllegalArgumentException("Invalid pet clip variant");
        // Keep the existing wire IDs compatible while retiring both paw washes.
        if(action==PetAction.GROOM)variant=2;
        Session session = new Session(player, pet, action, currentServer.overworld().getGameTime(), ++sequence, site,
            site == null ? null : pet.level().getBlockState(site),variant);
        sessions.put(pet.getUUID(), session);
        pet.getNavigation().stop();
        if (action.petting() || action==PetAction.HEAD_TILT) pet.getLookControl().setLookAt(player, 30, 30);
        if (site != null) {
            float yaw=(float)Math.toDegrees(Math.atan2(-(site.getX()+.5-pet.getX()),site.getZ()+.5-pet.getZ()));
            pet.setYRot(yaw);pet.yBodyRot=yaw;pet.yHeadRot=yaw;
            pet.getLookControl().setLookAt(site.getX()+.5, site.getY()+.5, site.getZ()+.5);
        }
        if(pet instanceof Cat cat){
            var sound=action==PetAction.STRETCH?net.minecraft.sounds.SoundEvents.CAT_PURREOW:net.minecraft.sounds.SoundEvents.CAT_PURR;
            float volume=action==PetAction.KNEAD?.3F:action==PetAction.GROOM?.18F:.55F;
            pet.level().playSound(null,pet,sound,SoundSource.NEUTRAL,volume,cat.isBaby()?1.25F:1);
        }else if((action==PetAction.DOG_PET||action==PetAction.HEAD_TILT)&&pet instanceof Wolf wolf){
            pet.level().playSound(null,pet,net.minecraft.sounds.SoundEvents.WOLF_PANT,SoundSource.NEUTRAL,action==PetAction.HEAD_TILT?.18F:.65F,wolf.isBaby()?1.35F:1.05F);
        }
        broadcast(session, 0, false);
    }
    public static boolean idleEligible(ServerPlayer owner, TamableAnimal pet) {
        return owner.isAlive() && !owner.isSpectator() && owner.level() == pet.level() && pet.isOwnedBy(owner)
            && pet.isAlive() && pet.isTame() && !pet.isOrderedToSit() && !pet.isInSittingPose()
            && pet.onGround() && !pet.isInWaterOrRain() && !pet.isInLava() && !pet.isPassenger()
            && !pet.isLeashed() && pet.hurtTime == 0 && pet.getTarget() == null && pet.getNavigation().isDone()
            && pet.getDeltaMovement().horizontalDistanceSqr() < .0001 && pet.distanceToSqr(owner) <= 256
            && (!(pet instanceof Wolf wolf) || (!wolf.isAngry() && !wolf.isBaby()))
            && (!(pet instanceof Cat cat) || (!cat.isLying() && cat.getLieDownAmount(1) < .01F));
    }
    /** A gaze reaction can be seated or a puppy; it never changes terrain or sitting state. */
    public static boolean reactionEligible(ServerPlayer owner,Wolf pet) {
        return owner.isAlive()&&!owner.isSpectator()&&owner.level()==pet.level()&&pet.isOwnedBy(owner)&&pet.isTame()&&pet.isAlive()
            &&pet.onGround()&&!pet.isInWaterOrRain()&&!pet.isInLava()&&!pet.isPassenger()&&!pet.isLeashed()
            &&pet.hurtTime==0&&pet.getTarget()==null&&!pet.isAngry()&&pet.getNavigation().isDone()
            &&pet.getDeltaMovement().horizontalDistanceSqr()<.0001&&pet.distanceToSqr(owner)<=36;
    }
    public static boolean lookingAt(ServerPlayer owner,Wolf pet) {
        var direction=pet.getEyePosition().subtract(owner.getEyePosition());
        return direction.lengthSqr()>.01&&owner.getLookAngle().dot(direction.normalize())>.985&&owner.hasLineOfSight(pet);
    }
    public static boolean mayReceive(ServerPlayer player,PetUpdate update) {
        return update.action()<6||expressive.contains(player.getUUID());
    }
    public static void tick(MinecraftServer server) {
        world(server);
        long now = server.overworld().getGameTime();
        for (Session session : new ArrayList<>(sessions.values())) {
            int elapsed = (int)(now - session.start);
            boolean valid = session.action.petting() ? canPet(session.player, session.pet)
                : modern.contains(session.player.getUUID())
                    && (session.action==PetAction.HEAD_TILT ? reactionEligible(session.player,(Wolf)session.pet) : idleEligible(session.player, session.pet))
                    && (!session.action.expressive()||expressive.contains(session.player.getUUID()))
                    && (session.action != PetAction.DIG ? world.stretching() : world.digging() && world.ownerDigging(session.player.getUUID()));
            if (!valid || session.pet.isRemoved() || elapsed >= session.action.duration) { stop(session); continue; }
            session.pet.getNavigation().stop();
            if (session.action.petting()||session.action==PetAction.HEAD_TILT) session.pet.getLookControl().setLookAt(session.player, 30, 30);
            if (session.action == PetAction.DIG) {
                if (elapsed >= 18 && elapsed < 60 && elapsed % 4 == 2) {
                    var level = (ServerLevel) session.pet.level();
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, session.original), session.site.getX()+.5,
                        session.site.getY()+1.05, session.site.getZ()+.5, 5, .22, .08, .22, .03);
                    var groundSound=session.original.getSoundType();
                    level.playSound(null, session.pet, groundSound.getHitSound(), SoundSource.NEUTRAL,
                        .85F*groundSound.getVolume(),groundSound.getPitch()*(elapsed%8==2?.9F:.96F));
                }
                if (elapsed >= 60 && !session.committed) {
                    session.committed = true;
                    var level = (ServerLevel)session.pet.level();
                    if (!DigSite.eligible(level, session.site) || !level.getBlockState(session.site).equals(session.original)
                        || !world.ownerReady(session.player.getUUID(), now) || !platform.mayDig(session.player, session.pet, session.site, true)) {
                        stop(session); continue;
                    }
                    // Cancellable hooks may change the world. Check the exact terrain again afterwards.
                    if(!DigSite.eligible(level,session.site)||!level.getBlockState(session.site).equals(session.original)){stop(session);continue;}
                    if (level.destroyBlock(session.site, true, session.pet)) {
                        world.dug(session.player.getUUID(), now);
                        if (world.finds()) find(session);
                    }
                }
            }
        }
        if (now % 20 == 0) {
            for (ServerPlayer owner : server.getPlayerList().getPlayers()) {
                if (!modern.contains(owner.getUUID()) || isPetting(owner.getUUID())) continue;
                for (TamableAnimal pet : owner.level().getEntitiesOfClass(TamableAnimal.class, owner.getBoundingBox().inflate(16))) {
                    if (!(pet instanceof Wolf || pet instanceof Cat) || !pet.isOwnedBy(owner) || sessions.containsKey(pet.getUUID())) continue;
                    if(pet instanceof Wolf wolf && expressive.contains(owner.getUUID()) && world.stretching()) {
                        long reactionDue=world.reactionDue(pet.getUUID());
                        if(reactionDue<0)world.scheduleReaction(pet.getUUID(),now+1200+pet.getRandom().nextInt(1201));
                        else if(reactionDue<=now&&reactionEligible(owner,wolf)&&lookingAt(owner,wolf)) {
                            world.scheduleReaction(pet.getUUID(),now+1200+pet.getRandom().nextInt(1201));
                            if(sessions.values().stream().noneMatch(s->s.player==owner)) {start(owner,pet,PetAction.HEAD_TILT,null);continue;}
                        }
                    }
                    long due = world.due(pet.getUUID());
                    if (due < 0) { schedule(pet, now); continue; }
                    if (due > now || !idleEligible(owner, pet)) continue;
                    schedule(pet, now); // An attempt consumes its interval even if the terrain is unsuitable.
                    if (sessions.values().stream().anyMatch(s -> s.player == owner)) continue;
                    if (pet instanceof Cat && world.stretching()) {
                        PetAction action=PetAction.STRETCH;
                        if(expressive.contains(owner.getUUID()))action=switch(pet.getRandom().nextInt(3)){case 1->PetAction.KNEAD;case 2->PetAction.GROOM;default->PetAction.STRETCH;};
                        start(owner, pet, action, null);
                    }
                    else if (pet instanceof Wolf && world.digging() && world.ownerDigging(owner.getUUID()) && world.ownerReady(owner.getUUID(), now)) {
                        BlockPos site = site(owner, pet);
                        if (site != null) start(owner, pet, PetAction.DIG, site);
                    }
                }
            }
        }
        if (now % 1200 == 0) world.save();
        nextAllowed.entrySet().removeIf(e -> now >= e.getValue() && !isPetting(e.getKey()));
    }
    private static void schedule(TamableAnimal pet, long now) {
        int minimum = pet instanceof Cat ? 3600 : 12000;
        world.schedule(pet.getUUID(), now + minimum + pet.getRandom().nextInt(minimum + 1));
    }
    private static BlockPos site(ServerPlayer owner, TamableAnimal pet) {
        var level = (ServerLevel)pet.level();
        BlockPos ground = pet.blockPosition().below();
        Direction forward = pet.getDirection();
        for (Direction d : new Direction[]{forward, forward.getClockWise(), forward.getCounterClockWise(), forward.getOpposite()}) {
            BlockPos pos = ground.relative(d);
            if (DigSite.eligible(level, pos) && platform.mayDig(owner, pet, pos, false)) return pos;
        }
        return null;
    }
    private static void find(Session s) {
        var level = (ServerLevel)s.pet.level();
        var key = ResourceKey.create(Registries.LOOT_TABLE, id("digging/finds"));
        var table = currentServer.reloadableRegistries().getLootTable(key);
        var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, s.pet.position())
            .withParameter(LootContextParams.THIS_ENTITY, s.pet).create(LootContextParamSets.GIFT);
        for (var item : table.getRandomItems(params)) if (!item.isEmpty())
            level.addFreshEntity(new ItemEntity(level, s.site.getX()+.5, s.site.getY()+1.05, s.site.getZ()+.5, item));
    }
    private static void broadcast(Session s, int elapsed, boolean stopped) {
        var update = update(s, elapsed, stopped);
        // Adapters choose the negotiated protocol for each recipient. Legacy clients receive dog petting only.
        platform.sendToTrackingAndSelf(s.player, s.pet, update);
        if (s.action == PetAction.DOG_PET) platform.sendToTrackingAndSelf(s.player, s.pet,
            new PetState(s.player.getUUID(), s.pet.getUUID(), stopped ? 0 : DURATION-elapsed));
    }
    private static PetUpdate update(Session s, int elapsed, boolean stopped) {
        return new PetUpdate(s.player.getUUID(), s.pet.getUUID(), stopped ? 0 : s.action.wireId,
            Math.max(0, Math.min(s.action.duration, elapsed)), s.sequence, stopped?0:s.variant, 0, "");
    }
    private static void stop(Session s) { broadcast(s, 0, true); sessions.remove(s.pet.getUUID(), s); }
    public static void disconnect(ServerPlayer player) {
        for (Session s : new ArrayList<>(sessions.values())) if (s.player.getUUID().equals(player.getUUID())) stop(s);
        modern.remove(player.getUUID());expressive.remove(player.getUUID()); nextAllowed.remove(player.getUUID()); controlAllowed.remove(player.getUUID());requestBudgets.remove(player.getUUID());
    }
    public static void syncTo(ServerPlayer observer, Entity entity) {
        if (currentServer == null) return;
        for (Session s : sessions.values()) if (entity == s.pet || entity == s.player) {
            int elapsed = (int)Math.max(0, Math.min(s.action.duration, currentServer.overworld().getGameTime()-s.start));
            if (modern.contains(observer.getUUID())) platform.sendToPlayer(observer, update(s, elapsed, false));
            else if (s.action == PetAction.DOG_PET) platform.sendToPlayer(observer, new PetState(s.player.getUUID(), s.pet.getUUID(), DURATION-elapsed));
        }
    }
    public static boolean isPetting(UUID player) { return sessions.values().stream().anyMatch(s -> s.player.getUUID().equals(player) && s.action.petting()); }
    public static boolean actionActive(TamableAnimal pet, boolean petting) { Session s = sessions.get(pet.getUUID()); return s != null && s.action.petting() == petting; }
    public static boolean reactionActive(TamableAnimal pet){Session s=sessions.get(pet.getUUID());return s!=null&&s.action==PetAction.HEAD_TILT;}
    public static void lookAtOwner(TamableAnimal pet){Session s=sessions.get(pet.getUUID());if(s!=null&&s.action==PetAction.HEAD_TILT)pet.getLookControl().setLookAt(s.player,30,30);}
    public static void clear() {
        if (world != null) world.save();
        sessions.clear(); nextAllowed.clear(); controlAllowed.clear(); modern.clear();expressive.clear();requestBudgets.clear(); world = null; currentServer = null; sequence = 0;
    }
    private static final class Session {
        final ServerPlayer player; final TamableAnimal pet; final PetAction action; final long start, sequence;
        final BlockPos site; final BlockState original; final int variant; boolean committed;
        Session(ServerPlayer player, TamableAnimal pet, PetAction action, long start, long sequence, BlockPos site, BlockState original,int variant) {
            this.player=player; this.pet=pet; this.action=action; this.start=start; this.sequence=sequence; this.site=site; this.original=original; this.variant=variant;
        }
    }
}
