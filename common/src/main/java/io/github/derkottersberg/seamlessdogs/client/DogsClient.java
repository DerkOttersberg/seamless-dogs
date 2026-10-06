package io.github.derkottersberg.seamlessdogs.client;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import io.github.derkottersberg.seamlessdogs.internal.ClientPlatformServices;
import io.github.derkottersberg.seamlessdogs.network.*;
import io.github.derkottersberg.seamlessdogs.gameplay.PetAction;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.phys.EntityHitResult;

public final class DogsClient {
    private static ClientPlatformServices platform;
    private static final Map<UUID,Clip> clips=new HashMap<>();
    private static final Map<UUID,Float> petHeights=new HashMap<>();
    private static final Map<UUID,Long> lastSequence=new HashMap<>();
    private static final Map<UUID,Long> stoppedSequence=new HashMap<>();
    private static Object previousLevel;
    private static Object previousPlayer;
    private static long nextRequest, nextHello;
    public record Settings(int flags,long revision,String status) {
        public boolean ready() { return revision>=0; }
        public boolean admin() { return (flags&16)!=0; }
        public boolean writable() { return (flags&32)!=0; }
    }
    public static Settings settings=new Settings(0,-1,"Join a server running Seamless Dogs 0.2.0 to edit pet behavior.");
    public static void initialize(ClientPlatformServices services) { platform=services;ClientOptions.load(services.configDirectory()); }
    private static void level() {
        var c=Minecraft.getInstance();
        if(previousLevel!=c.level || previousPlayer!=c.player) {
            clips.clear();petHeights.clear();lastSequence.clear();stoppedSequence.clear();nextRequest=nextHello=0;previousLevel=c.level;previousPlayer=c.player;
            settings=new Settings(0,-1,"Waiting for server settings.");
        }
    }
    private static long animationTick() {
        var player=Minecraft.getInstance().player;
        // World game time is periodically corrected by server packets and can
        // go backwards. Entity ticks follow the client's render interpolation.
        return player==null?0:Integer.toUnsignedLong(player.tickCount);
    }
    public static void receive(PetState state) {
        level(); var c=Minecraft.getInstance(); if(c.level==null || settings.ready()) return;
        if(state.remainingTicks()==0) release(state.dog());
        else clips.put(state.dog(),new Clip(state.player(),PetAction.DOG_PET,new AnimationTimeline.Playback(animationTick()-(40-state.remainingTicks())),0,0));
    }
    public static void receive(PetUpdate state) {
        level();var c=Minecraft.getInstance();if(c.level==null)return;
        if(state.action()==5) {
            settings=new Settings(state.flags(),state.revision(),state.status());
            if(c.gui.screen() instanceof DogsSettingsScreen screen) screen.serverSnapshot(settings);
            return;
        }
        long previous=lastSequence.getOrDefault(state.pet(),-1L);
        if(state.sequence()<=stoppedSequence.getOrDefault(state.pet(),-1L) || state.sequence()<previous || (state.sequence()==previous && !clips.containsKey(state.pet()))) return;
        lastSequence.put(state.pet(),state.sequence());
        if(state.action()==0) {stoppedSequence.put(state.pet(),state.sequence());release(state.pet());}
        else {
            var action=PetAction.fromWire(state.action());
            if(action!=null&&state.elapsed()<action.duration) clips.put(state.pet(),new Clip(state.owner(),action,new AnimationTimeline.Playback(animationTick()-state.elapsed()),state.sequence(),action==PetAction.GROOM&&state.flags()>=0&&state.flags()<=2?state.flags():0));
        }
    }
    public static void tick(Minecraft c) {
        level();
        while(DogsKeys.SETTINGS.consumeClick()) if(c.gui.screen()==null) c.setScreenAndShow(new DogsSettingsScreen(null));
        if(c.level==null || c.player==null) return;
        long now=c.level.getGameTime();
        if(!settings.ready() && platform.serverSupportsV2() && now>=nextHello) { refreshSettings();nextHello=now+100; }
        long visualNow=animationTick();
        clips.values().removeIf(clip->clip.timing.expired(visualNow,clip.action.duration));
        petHeights.keySet().retainAll(clips.keySet());
        while(platform.petKey().consumeClick()) {
            TamableAnimal pet=target();
            if(c.gui.screen()==null && pet!=null && now>=nextRequest) { platform.sendToServer(new PetRequest(pet.getId()));nextRequest=now+60; }
        }
    }
    public static void refreshSettings() { if(platform!=null && platform.serverSupportsV2()) platform.sendToServer(new PetControl(0,1,0)); }
    public static void saveSettings(boolean personal,int flags,long revision) { platform.sendToServer(new PetControl(personal?1:2,flags,revision)); }
    public static TamableAnimal target() {
        var c=Minecraft.getInstance();
        if(platform==null || !platform.serverSupportsPetting() || c.player==null || c.level==null || !c.player.getMainHandItem().isEmpty()
            || c.player.isSpectator() || c.player.isUsingItem() || c.player.isPassenger())return null;
        if(c.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof TamableAnimal pet && (pet instanceof Wolf || pet instanceof Cat)
            && (!(pet instanceof Cat cat) || (settings.ready() && !cat.isLying())) && pet.isTame() && pet.isOwnedBy(c.player) && pet.isAlive()
            && (!(pet instanceof Wolf wolf) || !wolf.isAngry()) && pet.getTarget()==null && pet.hurtTime==0
            && c.player.distanceToSqr(pet)<=9)return pet;
        return null;
    }
    public static PetAnimation.Sample playerSample(int id,float partial) {
        var c=Minecraft.getInstance();if(c.level==null || c.level.getEntity(id)==null)return PetAnimation.sample(-1);
        UUID owner=c.level.getEntity(id).getUUID();
        return clips.values().stream().filter(p->p.owner.equals(owner)&&p.action.petting()).findFirst().map(p->sample(p,partial)).orElse(PetAnimation.sample(-1));
    }
    public static PetAnimation.Sample petSample(UUID pet,float partial) {
        Clip clip=clips.get(pet);return clip==null || !clip.action.petting() ? PetAnimation.sample(-1) : sample(clip,partial);
    }
    public static PetAnimation.Sample dogSample(UUID pet,float partial) { return petSample(pet,partial); }
    private static PetAnimation.Sample sample(Clip clip,float partial) {
        var c=Minecraft.getInstance();if(clip==null||c.level==null)return PetAnimation.sample(-1);
        var sample=PetAnimation.sample(clip.timing.elapsed(animationTick(),partial));
        return new PetAnimation.Sample(sample.weight()*fade(clip,partial),sample.stroke());
    }
    public static AnimationClips.Pose actionPose(UUID pet,float partial) {
        var c=Minecraft.getInstance();Clip clip=clips.get(pet);
        if(c.level==null || clip==null)return AnimationClips.Pose.NONE;
        boolean kitten=false;
        for(var entity:c.level.entitiesForRendering())if(entity.getUUID().equals(pet)){kitten=entity instanceof Cat cat&&cat.isBaby();break;}
        return AnimationClips.sample(clip.action,clip.timing.elapsed(animationTick(),partial),kitten,clip.variant)
            .scaled(fade(clip,partial),kitten&&clip.action==PetAction.CAT_PET?.5F:1);
    }
    public static boolean stretchingEyes(UUID pet,float partial) {
        var c=Minecraft.getInstance();Clip clip=clips.get(pet);
        if(c.level==null||clip==null||!(clip.action==PetAction.STRETCH||clip.action==PetAction.KNEAD||clip.action==PetAction.GROOM))return false;
        float elapsed=clip.timing.elapsed(animationTick(),partial);
        return elapsed>=12&&elapsed<clip.action.duration-12&&fade(clip,partial)>.25F;
    }
    public static boolean bendToPet(int playerId) {
        var c=Minecraft.getInstance();if(c.level==null||c.level.getEntity(playerId)==null)return false;
        UUID owner=c.level.getEntity(playerId).getUUID();
        for(var clip:clips.values())if(clip.owner.equals(owner)&&clip.action==PetAction.CAT_PET)return true;
        return false;
    }
    public static float handHeightBias(int playerId) {
        var c=Minecraft.getInstance();if(c.level==null || c.level.getEntity(playerId)==null)return 0;
        UUID owner=c.level.getEntity(playerId).getUUID();
        for(var entry:clips.entrySet()) if(entry.getValue().owner.equals(owner)&&entry.getValue().action.petting()) {
            for(var entity:c.level.entitiesForRendering()) if(entity.getUUID().equals(entry.getKey())) {
                float height=Math.max(-.4F,Math.min(.3F,entity.getBbHeight()-.8F));
                petHeights.put(entry.getKey(),height);return height;
            }
            // Keep the last contact height during recovery if the pet unloads.
            return petHeights.getOrDefault(entry.getKey(),0F);
        }
        return 0;
    }
    public static void prompt(GuiGraphicsExtractor graphics) {
        var c=Minecraft.getInstance();TamableAnimal pet=target();
        if(!ClientOptions.prompt || c.gui.hud.isHidden() || c.gui.screen()!=null || pet==null)return;
        boolean active=clips.values().stream().anyMatch(p->p.owner.equals(c.player.getUUID())&&p.action.petting());
        boolean cooldown=c.level.getGameTime()<nextRequest;
        Component text=Component.translatable(active ? pet instanceof Cat ? "seamlessdogs.petting_cat" : "seamlessdogs.petting"
            : cooldown ? "seamlessdogs.cooldown" : pet instanceof Cat ? "seamlessdogs.prompt_cat" : "seamlessdogs.prompt",platform.petKey().getTranslatedKeyMessage());
        int width=c.font.width(text),x=graphics.guiWidth()/2,y=graphics.guiHeight()-58;
        graphics.fill(x-width/2-7,y-4,x+width/2+7,y+13,0xB0182125);
        graphics.centeredText(c.font,text,x,y,active?0xFFB7E8BD:0xFFF4EEE4);
    }
    private static void release(UUID pet) {
        var c=Minecraft.getInstance();if(c.level==null){clips.remove(pet);return;}
        var clip=clips.get(pet);if(clip!=null)clip.timing.stop(animationTick());
    }
    private static float fade(Clip clip,float partial) {
        return clip.timing.fade(animationTick(),partial);
    }
    private record Clip(UUID owner,PetAction action,AnimationTimeline.Playback timing,long sequence,int variant) { }
}
