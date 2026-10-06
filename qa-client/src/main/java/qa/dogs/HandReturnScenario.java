package qa.dogs;

import io.github.derkottersberg.seamlessdogs.client.DogsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.wolf.Wolf;

/** Server interruption stays on the real action-stop packet path in a disposable fixture. */
public final class HandReturnScenario {
    private int scenario=-1,ticks,width,height,frameLimit;
    public boolean tick(Minecraft c) {
        if(c.player==null||c.level==null)return false;
        Wolf dog=null;for(var e:c.level.entitiesForRendering())if(e instanceof Wolf w&&w.isOwnedBy(c.player)){dog=w;break;}
        if(dog==null)return false;
        var d=dog.getEyePosition().subtract(c.player.getEyePosition());
        c.player.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));
        c.player.setXRot((float)-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z))));
        c.options.setCameraType(CameraType.FIRST_PERSON);
        ticks++;
        if(scenario<0&&ticks==1){
            // Preserve real clip timing while the bounded software renderer
            // samples a small viewport. Restore the normal capture size below.
            width=c.getWindow().getWidth();height=c.getWindow().getHeight();frameLimit=c.options.framerateLimit().get();
            c.getWindow().setWindowed(640,480);c.options.framerateLimit().set(60);
        }
        if(scenario<0){
            // Completed-frame screenshots stall software GPU readback. The
            // visual-only fixture may allow a slow-motion command. Assertions
            // stay unchanged if world permissions reject it; other gates use 20 TPS.
            if(ticks==1)c.getConnection().sendCommand("tick rate 10");
            if(ticks>85){scenario=0;ticks=0;c.getConnection().sendCommand("dogsqa dog_adult");}return false;
        }
        if(ticks==3) {
            c.options.mainHand().set(scenario<2?HumanoidArm.RIGHT:HumanoidArm.LEFT);c.options.broadcastOptions();
            String label=switch(scenario){case 0->"right-natural";case 1->"right-interrupted";case 2->"left-interrupted";default->"left-natural-swing";};
            HandReturnProbe.start(label,scenario==1||scenario==2);
            c.getConnection().sendCommand("dogsqa pet_now");
        }
        if(ticks==23&&(scenario==1||scenario==2))c.getConnection().sendCommand("dogsqa interrupt_pet");
        if(ticks==38&&scenario==0||ticks==26&&scenario==1)c.getConnection().sendCommand("dogsqa rewind_client_time");
        if(ticks==38&&scenario==3)c.player.swing(InteractionHand.MAIN_HAND,net.minecraft.world.item.component.SwingAnimation.DEFAULT,false);
        if(ticks>76) {
            HandReturnProbe.requireComplete();
            if(DogsClient.playerSample(c.player.getId(),0).weight()!=0)throw new IllegalStateException("Hand still raised after recovery");
            if(++scenario==4){HandReturnProbe.finish(c);c.getWindow().setWindowed(width,height);c.options.framerateLimit().set(frameLimit);c.getConnection().sendCommand("tick rate 20");return true;}
            ticks=0;
        }
        return false;
    }
}
