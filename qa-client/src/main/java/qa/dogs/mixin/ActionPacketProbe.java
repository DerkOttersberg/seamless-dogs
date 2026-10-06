package qa.dogs.mixin;
import io.github.derkottersberg.seamlessdogs.client.DogsClient;
import io.github.derkottersberg.seamlessdogs.network.PetUpdate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(DogsClient.class)
public abstract class ActionPacketProbe {
    @Inject(method="receive(Lio/github/derkottersberg/seamlessdogs/network/PetUpdate;)V",at=@At("HEAD"))
    private static void qa$action(PetUpdate state,CallbackInfo ci) {
        if(state.action()==7)qa.dogs.PetFeatureProbe.expectedGrooms.put(state.pet(),state.flags());
        else if(state.action()!=5)qa.dogs.PetFeatureProbe.expectedGrooms.remove(state.pet());
        if(state.action()==2&&state.elapsed()>0)qa.dogs.PetFeatureProbe.lateCats++;
        if(state.action()==3)qa.dogs.PetFeatureProbe.remoteDig++;
        if(state.action()==4)qa.dogs.PetFeatureProbe.remoteStretch++;
        if(state.action()==6)qa.dogs.PetFeatureProbe.remoteKnead++;
        if(state.action()==7){qa.dogs.PetFeatureProbe.remoteGroom++;qa.dogs.PetFeatureProbe.groomVariants|=1<<state.flags();if(state.elapsed()>0)qa.dogs.PetFeatureProbe.lateGroomVariants|=1<<state.flags();}
        if(state.action()==8)qa.dogs.PetFeatureProbe.remoteTilt++;
        if(state.action()>=6&&state.elapsed()>0)qa.dogs.PetFeatureProbe.lateExpressions++;
        if(state.action()==5&&state.status().contains("not saved"))qa.dogs.PetFeatureProbe.rejectedSettings++;
    }
}
