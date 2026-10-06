package qa.dogs;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import java.nio.file.Files;
import java.util.Arrays;

/** Exercises native controls and options persistence in the packaged client. */
public final class RebindProbe {
    private int phase,ticks,prompts;
    private DogsSettingsScreen parent;
    public boolean tick(Minecraft c) {
        if(phase==3)return true;
        if(phase==0) {
            check(Arrays.asList(c.options.keyMappings).contains(DogsKeys.PET),"Pet key not registered");
            check(Arrays.asList(c.options.keyMappings).contains(DogsKeys.SETTINGS),"Settings key not registered");
            parent=new DogsSettingsScreen(null);c.setScreen(parent);
            openControls(c);
            var screen=(KeyBindsScreen)c.screen;
            screen.selectedKey=DogsKeys.PET;keyboard(screen,InputConstants.KEY_H);
            persisted(c,"key.keyboard.h");
            KeyMapping.click(InputConstants.getKey("key.keyboard.h"));check(DogsKeys.PET.consumeClick(),"Rebound keyboard dispatch failed");
            screen.selectedKey=DogsKeys.PET;screen.mouseClicked(0,0,4);
            persisted(c,InputConstants.Type.MOUSE.getOrCreate(4).getName());
            KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(4));check(DogsKeys.PET.consumeClick(),"Mouse dispatch failed");
            screen.selectedKey=DogsKeys.PET;keyboard(screen,InputConstants.KEY_ESCAPE);
            persisted(c,"key.keyboard.unknown");check(DogsKeys.PET.isUnbound(),"Escape did not unbind");
            screen.onClose();check(c.screen==parent,"Controls lost settings parent");
            checkButton(c);c.setScreen(null);prompts=ClientProbe.prompts;phase=1;ticks=0;
            KeyMapping.click(InputConstants.getKey("key.keyboard.h"));
            return false;
        }
        if(phase==1 && ++ticks>8) {
            check(ClientProbe.prompts==prompts,"Unbound action still shows a pet prompt");
            check(DogsClient.playerSample(c.player.getId(),0).weight()==0,"Old binding activated an unbound action");
            c.setScreen(parent);openControls(c);var screen=(KeyBindsScreen)c.screen;
            screen.selectedKey=DogsKeys.PET;
            if(Boolean.getBoolean("qa.petMouse")){screen.mouseClicked(0,0,4);}else keyboard(screen,InputConstants.KEY_H);
            persisted(c,Boolean.getBoolean("qa.petMouse")?InputConstants.Type.MOUSE.getOrCreate(4).getName():"key.keyboard.h");
            screen.onClose();checkButton(c);c.setScreen(null);
            KeyMapping.click(InputConstants.getKey("key.keyboard.g"));phase=2;ticks=0;return false;
        }
        if(phase==2 && ++ticks>8) {
            check(DogsClient.playerSample(c.player.getId(),0).weight()==0,"Old default G still activated petting");
            check(ClientProbe.prompts>prompts,"Rebound prompt did not recover");
            try{Files.writeString(c.gameDirectory.toPath().resolve("dogs-rebind-passed.txt"),"PASS native keyboard/mouse/Escape, registration, options save/load, unbound prompt suppression, old default rejection; gameplay continues on "+DogsKeys.PET.saveString()+"\n");}catch(Exception e){throw new RuntimeException(e);}
            ClientProbe.log("DOGS_REBIND_PASS "+DogsKeys.PET.saveString());phase=3;return true;
        }
        return false;
    }
    private static void keyboard(KeyBindsScreen screen,int key){screen.keyPressed(key,0,0);screen.keyReleased(key,0,0);}
    private static void persisted(Minecraft c,String expected){
        check(DogsKeys.PET.saveString().equals(expected),"Native binding mismatch: "+DogsKeys.PET.saveString());
        c.options.save();DogsKeys.PET.setKey(DogsKeys.PET.getDefaultKey());KeyMapping.resetMapping();c.options.load();KeyMapping.resetMapping();
        check(DogsKeys.PET.saveString().equals(expected),"Binding did not survive options reload");
    }
    private static void openControls(Minecraft c){
        for(var child:c.screen.children())if(child instanceof Button b&&b.getMessage().getString().startsWith("Petting key:")){b.onPress(); check(c.screen instanceof KeyBindsScreen,"Settings button failed to open native controls");return;}
        throw new IllegalStateException("Missing petting key settings button");
    }
    private static void checkButton(Minecraft c){
        for(var child:c.screen.children())if(child instanceof Button b&&b.getMessage().getString().startsWith("Petting key:")){check(b.getMessage().getString().contains(DogsKeys.PET.getTranslatedKeyMessage().getString()),"Settings label stale");check(b.getY()+b.getHeight()<=c.screen.height-28,"Binding button overlaps footer");return;}
        throw new IllegalStateException("Missing returned binding button");
    }
    private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
}
