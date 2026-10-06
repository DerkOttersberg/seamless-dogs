package qa.dogs.mixin;
import qa.dogs.ClientProbe;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPacketListener.class)
public abstract class SoundProbe {
    @Inject(method="handleSoundEntityEvent",at=@At("TAIL"))
    private void qa$sound(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        String sound=packet.getSound().value().getLocation().getPath();
        if(sound.equals("block.grass.hit")||sound.equals("block.sand.hit")){
            if(packet.getVolume()<.8F)throw new IllegalStateException("Dig scrape too quiet: "+packet.getVolume());
            var c=net.minecraft.client.Minecraft.getInstance();
            if(c.level==null||!(c.level.getEntity(packet.getId()) instanceof net.minecraft.world.entity.animal.Wolf))throw new IllegalStateException("Dig sound is not attached to the dog");
            qa.dogs.PetFeatureProbe.digSounds.merge(sound,1,Integer::sum);
        }
        if(packet.getSound().value().getLocation().getPath().contains("cat") && (packet.getSound().value().getLocation().getPath().contains("purr"))) { qa.dogs.PetFeatureProbe.catSounds++;qa.dogs.PetFeatureProbe.soundCat=packet.getId(); }
        if (packet.getSound().value().getLocation().getPath().contains("wolf") && packet.getSound().value().getLocation().getPath().contains("pant")) {
            ClientProbe.sounds++; ClientProbe.soundDog = packet.getId(); ClientProbe.log("entity-bound pant packet dog=" + packet.getId());
        }
    }
}
