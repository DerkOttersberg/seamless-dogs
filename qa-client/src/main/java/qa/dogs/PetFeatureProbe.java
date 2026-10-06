package qa.dogs;

import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.core.BlockPos;
import java.nio.file.Files;

/** Real production key/packet/render/settings paths, accelerated only by the test-only server fixture. */
public final class PetFeatureProbe {
    public static final java.util.Map<java.util.UUID,Integer> expectedGrooms=new java.util.HashMap<>();
    public static int lateCats,rejectedSettings,remoteDig,remoteStretch;
    public static int remoteKnead,remoteGroom,remoteTilt,lateExpressions,groomVariants,lateGroomVariants,renderedGroomVariants;
    public static int catFrames,catEyes,catSounds,soundCat,catPoses,digPoses;
    public static final java.util.Map<String,Integer> digSounds=new java.util.HashMap<>();
    private int phase,ticks,total,start;
    private Cat cat;private Wolf dog;
    private long previousRevision;
    private boolean done;
    private boolean stretched;
    private boolean adultStretched;
    private boolean backCaptured;
    private boolean expressionRendered;
    private boolean eyesValidated;
    public boolean tick(Minecraft c) {
        if(done)return true;if(++total>4800)throw new IllegalStateException("Pet feature timeout phase="+phase);
        if(c.level==null||c.player==null)return false;
        if(cat==null||cat.isRemoved())for(var e:c.level.entitiesForRendering())if(e instanceof Cat candidate&&candidate.isOwnedBy(c.player)){cat=candidate;break;}
        if(dog==null||dog.isRemoved())for(var e:c.level.entitiesForRendering())if(e instanceof Wolf candidate&&candidate.isOwnedBy(c.player)){dog=candidate;break;}
        var target=phase>=9&&phase<30||(phase==34||phase==35)?dog:cat;
        if(target!=null){var d=target.getEyePosition().subtract(c.player.getEyePosition());c.player.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));c.player.setXRot((float)-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z))));}
        if(phase==4&&ticks>3){c.player.setYRot(c.player.getYRot()-35);c.player.setXRot(20);}
        ticks++;
        var petting=DogsClient.playerSample(c.player.getId(),0);
        switch(phase) {
            case 0 -> {if(!eyesValidated){CatEyeProbe.verify(c);eyesValidated=true;}if(ticks>65){command(c,"cat");next();}}
            case 1 -> {if(cat!=null&&DogsClient.target()==cat){c.options.mainHand().set(net.minecraft.world.entity.HumanoidArm.RIGHT);c.options.broadcastOptions();catFrames=catEyes=catSounds=ClientProbe.hands=0;click();next();}}
            case 2 -> {if(petting.weight()>.8F&&catFrames>0&&catEyes>0&&catSounds>0&&soundCat==cat.getId()&&ClientProbe.hands>0){capture(c,"08-cat-first-person.png");if("owner".equals(System.getProperty("qa.role")))command(c,"observer_far");start=total;next();}}
            case 3 -> {if(ticks%4==0&&petting.weight()>.1F)capture(c,"08-cat-hand-"+ticks+".png");if(total-start>65){c.options.setCameraType(CameraType.THIRD_PERSON_BACK);ClientProbe.playerModels=0;click();next();}}
            case 4 -> {if(ticks==8&&"owner".equals(System.getProperty("qa.role")))command(c,"observer_near");if(petting.weight()>.8F&&ClientProbe.playerModels>0&&ticks>12&&!backCaptured){capture(c,"09-cat-third-person.png");backCaptured=true;c.options.setCameraType(CameraType.THIRD_PERSON_FRONT);}if(backCaptured&&ticks>23){capture(c,"09-cat-third-person-front.png");start=total;next();}}
            case 5 -> {if(total-start>65){c.options.setCameraType(CameraType.FIRST_PERSON);c.options.mainHand().set(net.minecraft.world.entity.HumanoidArm.LEFT);c.options.broadcastOptions();command(c,"kitten");next();}}
            case 6 -> {if(cat.isBaby()&&DogsClient.target()==cat){catFrames=catEyes=catSounds=ClientProbe.hands=0;click();next();}}
            case 7 -> {if(petting.weight()>.8F&&catFrames>0&&catEyes>0&&catSounds>0&&ClientProbe.hands>0){capture(c,"10-kitten.png");start=total;next();}}
            case 8 -> {
                if(total-start>65&&ticks==66){command(c,"view_side");command(c,"stretch_now");}
                if(total-start>65&&!DogsClient.actionPose(cat.getUUID(),0).parts().isEmpty()){
                    if(cat.isBaby())stretched=true;else adultStretched=true;
                    if(ticks%6==0)capture(c,"11-stretch-"+(cat.isBaby()?"kitten":"adult")+"-"+ticks+".png");
                }
                    if(ticks==180){if(!stretched||catPoses==0)throw new IllegalStateException("Kitten stretch never rendered on its native rig");if(!DogsClient.actionPose(cat.getUUID(),0).parts().isEmpty())throw new IllegalStateException("Kitten stretch did not recover");command(c,"adult");command(c,"stretch_now");}
                if(ticks>310){if(!adultStretched)throw new IllegalStateException("Adult stretch never rendered");if(!DogsClient.actionPose(cat.getUUID(),0).parts().isEmpty())throw new IllegalStateException("Adult stretch did not recover");command(c,"knead");phase=30;ticks=0;expressionRendered=false;}
            }
            case 30,32,34,35,37,39 -> {
                if("owner".equals(System.getProperty("qa.role"))&&(phase==30||phase==34||phase==37)) {
                    if(ticks==(phase==34?8:20))command(c,"observer_far");
                    if(ticks==(phase==34?30:45))command(c,"observer_near");
                }
                var pet=(phase==34||phase==35)?dog:cat;
                var pose=DogsClient.actionPose(pet.getUUID(),0);
                String name=switch(phase){case 30->"knead-adult";case 32->"knead-kitten";case 34->"tilt-adult";case 35->"tilt-puppy";case 37->"groom-chest-adult";default->"groom-chest-kitten";};
                if(ticks==2)command(c,(phase==30||phase==32||phase==37||phase==39)?"view_front":"view_side");

                if(ticks==48&&(phase==30||phase==32||phase==37||phase==39))command(c,"view_side");
                if(!pose.parts().isEmpty()){expressionRendered=true;if(ticks%6==0)capture(c,"16-"+name+"-"+ticks+".png");}
                if(ticks>((phase==34||phase==35)?95:150)) {
                    if(!expressionRendered)throw new IllegalStateException(name+" never rendered");
                    if(!pose.parts().isEmpty())throw new IllegalStateException(name+" did not recover");
                    expressionRendered=false;ticks=0;
                    switch(phase){
                        case 30->{command(c,"kitten");command(c,"knead");phase=31;}
                        case 32->{command(c,"adult");command(c,"groom_chest");phase=36;}
                        case 37->{command(c,"kitten");command(c,"groom_chest");phase=38;}
                        case 39->{command(c,"dog_adult");command(c,"tilt_due");phase=33;}
                        case 34->{command(c,"puppy");command(c,"tilt_due");}
                        default->{command(c,"dig");phase=8;}
                    }
                    phase++;
                }
            }
            case 9 -> {if(!DogsClient.actionPose(dog.getUUID(),0).parts().isEmpty()){capture(c,"12-dig-start.png");next();}}
            case 10 -> {
                if(ticks%4==0&&ticks<80)capture(c,"13-dig-"+ticks+".png");
                if(ticks>105){if(digPoses==0)throw new IllegalStateException("Dig never rendered on its native rig");if(!c.level.getBlockState(new BlockPos(0,64,1)).isAir())throw new IllegalStateException("Dog did not remove its eligible block");
                    if(!DogsClient.actionPose(dog.getUUID(),0).parts().isEmpty())throw new IllegalStateException("Dig did not recover");
                    if(digSounds.getOrDefault("block.grass.hit",0)!=11)throw new IllegalStateException("Missing alternating dirt/grass scrapes: "+digSounds);
                    command(c,"dig_sand");phase=40;ticks=0;}
            }
            case 40 -> {if(ticks>125){
                if(digSounds.getOrDefault("block.sand.hit",0)!=11||!c.level.getBlockState(new BlockPos(0,64,1)).isAir())throw new IllegalStateException("Missing sand dig/block-specific sound: "+digSounds);
                c.setScreenAndShow(new DogsSettingsScreen(null));phase=11;ticks=0;}}
            case 11 -> {if(ticks==11)press(c,"My pets");if(ticks>16){capture(c,"14-owner-settings.png");previousRevision=DogsClient.settings.revision();pressPrefix(c,"My dogs can dig:");press(c,"Save");next();}}
            case 12 -> {if(DogsClient.settings.revision()>previousRevision&&(DogsClient.settings.flags()&8)==0){press(c,"Cancel");command(c,"dig");next();}}
            case 13 -> {if(ticks>100){if(!DogsClient.actionPose(dog.getUUID(),0).parts().isEmpty()||c.level.getBlockState(new BlockPos(0,64,1)).isAir())throw new IllegalStateException("Owner opt-out failed");
                    c.setScreenAndShow(new DogsSettingsScreen(null));next();}}
            case 14 -> {if(ticks>10){press(c,"My pets");previousRevision=DogsClient.settings.revision();pressPrefix(c,"My dogs can dig:");press(c,"Save");next();}}
            case 15 -> {if(DogsClient.settings.revision()>previousRevision&&(DogsClient.settings.flags()&8)!=0){press(c,"Cancel");
                if(DogsClient.settings.admin()){c.setScreenAndShow(new DogsSettingsScreen(null));next();}else{command(c,"dig_cooldown");phase=20;ticks=0;}}}
            case 16 -> {if(ticks==11)press(c,"World/server");if(ticks>16){capture(c,"15-server-settings.png");previousRevision=DogsClient.settings.revision();pressPrefix(c,"Dogs can dig:");press(c,"Save");next();}}
            case 17 -> {if(DogsClient.settings.revision()>previousRevision&&(DogsClient.settings.flags()&1)==0){press(c,"Cancel");command(c,"dig");next();}}
            case 18 -> {if(ticks>100){if(!DogsClient.actionPose(dog.getUUID(),0).parts().isEmpty()||c.level.getBlockState(new BlockPos(0,64,1)).isAir())throw new IllegalStateException("Server digging switch failed");c.setScreenAndShow(new DogsSettingsScreen(null));next();}}
            case 19 -> {if(ticks>10){press(c,"World/server");previousRevision=DogsClient.settings.revision();pressPrefix(c,"Dogs can dig:");press(c,"Save");phase=21;ticks=0;}}
            case 20 -> {if(ticks>100){if(!DogsClient.actionPose(dog.getUUID(),0).parts().isEmpty()||c.level.getBlockState(new BlockPos(0,64,1)).isAir())throw new IllegalStateException("Owner cooldown bypassed");finish(c);}}
            case 21 -> {if(DogsClient.settings.revision()>previousRevision&&(DogsClient.settings.flags()&1)!=0){press(c,"Cancel");command(c,"dig_cooldown");phase=20;ticks=0;}}
        }
        return done;
    }
    private void finish(Minecraft c){if(renderedGroomVariants!=4)throw new IllegalStateException("Chest grooming did not render");try{Files.writeString(c.gameDirectory.toPath().resolve("pets-feature-passed.txt"),"PASS cat/kitten petting, native rigs, eyes, purr, first/third person, stretch, chest grooming with retired paw washes absent, kneading, head tilts, terrain removal, eleven dog-bound grass/sand scrapes each, owner/server switches and owner cooldown.\n");}catch(Exception e){throw new RuntimeException(e);}done=true;ClientProbe.log("PETS_FEATURE_PASS");}
    private void next(){phase++;ticks=0;ClientProbe.log("pets phase="+phase);}
    private static void command(Minecraft c,String name){c.getConnection().sendCommand("dogsqa "+name);}
    private static void click(){KeyMapping.click(InputConstants.getKey(DogsKeys.PET.saveString()));}
    private static void press(Minecraft c,String name){pressPrefix(c,name);}
    private static void pressPrefix(Minecraft c,String name){for(var child:c.gui.screen().children())if(child instanceof Button b&&b.getMessage().getString().startsWith(name)){if(!b.active)throw new IllegalStateException("Disabled QA control "+name);b.onPress(null);return;}throw new IllegalStateException("Missing QA control "+name);}
    private static void capture(Minecraft c,String name){Screenshot.grab(c.gameDirectory,name,c.gameRenderer.mainRenderTarget(),1,m->ClientProbe.log(m.getString()));}
}
