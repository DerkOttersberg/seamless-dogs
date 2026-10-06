package qa.dogs;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import java.nio.file.Files;
import java.util.Arrays;

/** Exercises native controls and options persistence in the packaged client. */
public final class RebindProbe {
    public static long renderedFrames;
    private int phase,ticks,prompts;
    private long frameStart,deadline;
    private DogsSettingsScreen parent;
    public boolean tick(Minecraft c) {
        if(phase==3)return true;
        if(phase==0) {
            check(Arrays.asList(c.options.keyMappings).contains(DogsKeys.PET),"Pet key not registered");
            check(Arrays.asList(c.options.keyMappings).contains(DogsKeys.SETTINGS),"Settings key not registered");
            parent=new DogsSettingsScreen(null);c.setScreenAndShow(parent);
            openControls(c);
            var screen=(KeyBindsScreen)c.gui.screen();
            screen.selectedKey=DogsKeys.PET;keyboard(screen,InputConstants.KEY_H);
            persisted(c,"key.keyboard.h");
            KeyMapping.click(InputConstants.getKey("key.keyboard.h"));check(DogsKeys.PET.consumeClick(),"Rebound keyboard dispatch failed");
            screen.selectedKey=DogsKeys.PET;screen.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(0,0,new net.minecraft.client.input.MouseButtonInfo(4,0)),false);
            persisted(c,InputConstants.Type.MOUSE.getOrCreate(4).getName());
            KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(4));check(DogsKeys.PET.consumeClick(),"Mouse dispatch failed");
            screen.selectedKey=DogsKeys.PET;keyboard(screen,InputConstants.KEY_ESCAPE);
            persisted(c,"key.keyboard.unknown");check(DogsKeys.PET.isUnbound(),"Escape did not unbind");
            screen.onClose();check(c.gui.screen()==parent,"Controls lost settings parent");
            checkButton(c);c.setScreenAndShow(null);prompts=ClientProbe.prompts;phase=1;beginWait();
            KeyMapping.click(InputConstants.getKey("key.keyboard.h"));
            return false;
        }
        if(phase==1 && ++ticks>8) {
            if(renderedFrames-frameStart<3){check(System.nanoTime()<deadline,"No completed frames while testing an unbound prompt");return false;}
            check(ClientProbe.prompts==prompts,"Unbound action still shows a pet prompt");
            check(DogsClient.playerSample(c.player.getId(),0).weight()==0,"Old binding activated an unbound action");
            c.setScreenAndShow(parent);openControls(c);var screen=(KeyBindsScreen)c.gui.screen();
            screen.selectedKey=DogsKeys.PET;
            if(Boolean.getBoolean("qa.petMouse")){screen.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(0,0,new net.minecraft.client.input.MouseButtonInfo(4,0)),false);}else keyboard(screen,InputConstants.KEY_H);
            persisted(c,Boolean.getBoolean("qa.petMouse")?InputConstants.Type.MOUSE.getOrCreate(4).getName():"key.keyboard.h");
            screen.onClose();checkButton(c);c.setScreenAndShow(null);
            KeyMapping.click(InputConstants.getKey("key.keyboard.g"));phase=2;beginWait();return false;
        }
        if(phase==2 && ++ticks>8) {
            check(DogsClient.playerSample(c.player.getId(),0).weight()==0,"Old default G still activated petting");
            if(renderedFrames-frameStart<3||ClientProbe.prompts<=prompts){
                check(System.nanoTime()<deadline,"Rebound prompt did not recover: frames="+(renderedFrames-frameStart)+" prompts="+ClientProbe.prompts+" initial="+prompts+" key="+DogsKeys.PET.saveString()+" target="+DogsClient.target()+" enabled="+ClientOptions.prompt);
                return false;
            }
            try{Files.writeString(c.gameDirectory.toPath().resolve("dogs-rebind-passed.txt"),"PASS native keyboard/mouse/Escape, registration, options save/load, unbound prompt suppression, old default rejection; gameplay continues on "+DogsKeys.PET.saveString()+"\n");}catch(Exception e){throw new RuntimeException(e);}
            ClientProbe.log("DOGS_REBIND_PASS "+DogsKeys.PET.saveString());phase=3;return true;
        }
        return false;
    }
    private void beginWait(){ticks=0;frameStart=renderedFrames;deadline=System.nanoTime()+20_000_000_000L;}
    private static void keyboard(KeyBindsScreen screen,int key){
        var event=new net.minecraft.client.input.KeyEvent(key,0,0);
        screen.keyPressed(event);screen.keyReleased(event);
    }
    private static void persisted(Minecraft c,String expected){
        check(DogsKeys.PET.saveString().equals(expected),"Native binding mismatch: "+DogsKeys.PET.saveString());
        c.options.save();DogsKeys.PET.setKey(DogsKeys.PET.getDefaultKey());KeyMapping.resetMapping();c.options.load();KeyMapping.resetMapping();
        check(DogsKeys.PET.saveString().equals(expected),"Binding did not survive options reload");
    }
    private static void openControls(Minecraft c){
        for(var child:c.gui.screen().children())if(child instanceof Button b&&b.getMessage().getString().startsWith("Petting key:")){b.onPress(null); check(c.gui.screen() instanceof KeyBindsScreen,"Settings button failed to open native controls");return;}
        throw new IllegalStateException("Missing petting key settings button");
    }
    private static void checkButton(Minecraft c){
        for(var child:c.gui.screen().children())if(child instanceof Button b&&b.getMessage().getString().startsWith("Petting key:")){check(b.getMessage().getString().contains(DogsKeys.PET.getTranslatedKeyMessage().getString()),"Settings label stale");check(b.getY()+b.getHeight()<=c.gui.screen().height-28,"Binding button overlaps footer");return;}
        throw new IllegalStateException("Missing returned binding button");
    }
    private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
}
