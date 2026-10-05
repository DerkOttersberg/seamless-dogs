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
        if (packet.getSound().value().getLocation().getPath().contains("wolf") && packet.getSound().value().getLocation().getPath().contains("pant")) {
            ClientProbe.sounds++; ClientProbe.soundDog = packet.getId(); ClientProbe.log("entity-bound pant packet dog=" + packet.getId());
        }
    }
}
