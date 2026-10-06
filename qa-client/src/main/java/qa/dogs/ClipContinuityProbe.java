package qa.dogs;

import io.github.derkottersberg.seamlessdogs.client.DogsClient;
import io.github.derkottersberg.seamlessdogs.network.PetUpdate;
import java.util.*;

/** Exercises production packet handlers against delayed tracking and idle-to-pet transitions. */
public final class ClipContinuityProbe {
    private static final Map<UUID,PetUpdate> latest=new HashMap<>();
    private static UUID interrupt;
    private static Map<String,float[]> before;
    private static boolean passed;
    public static void receiveHead(PetUpdate state) {
        if(state.action()!=0 && state.action()!=5)latest.put(state.pet(),state);
        if(state.action()==2 && state.pet().equals(interrupt))before=DogsClient.actionPose(interrupt,0).parts();
    }
    public static void receiveTail(PetUpdate state) {
        if(state.action()!=2 || !state.pet().equals(interrupt))return;
        var after=DogsClient.actionPose(interrupt,0).parts();
        float magnitude=0,delta=0;
        Set<String> keys=new HashSet<>(before.keySet());keys.addAll(after.keySet());
        for(var name:keys)for(int i=0;i<6;i++){
            float a=before.getOrDefault(name,new float[6])[i],b=after.getOrDefault(name,new float[6])[i];
            magnitude=Math.max(magnitude,Math.abs(a));delta=Math.max(delta,Math.abs(a-b));
        }
        if(magnitude<1 || delta>Math.max(.4F,magnitude*.25F))throw new IllegalStateException("Idle pose snapped on petting: before="+magnitude+" delta="+delta);
        passed=true;interrupt=null;ClientProbe.log("PASS actual idle-to-pet packet preserves outgoing pose");
    }
    public static void replay(UUID pet) {
        var state=latest.get(pet);if(state==null)throw new IllegalStateException("Missing real action packet for replay");
        var beforePet=DogsClient.petSample(pet,.25F);var beforePose=DogsClient.actionPose(pet,.25F).parts();
        DogsClient.receive(new PetUpdate(state.owner(),pet,state.action(),0,state.sequence(),state.flags(),state.revision(),state.status()));
        var afterPet=DogsClient.petSample(pet,.25F);var afterPose=DogsClient.actionPose(pet,.25F).parts();
        if(!beforePet.equals(afterPet) || !beforePose.keySet().equals(afterPose.keySet()))throw new IllegalStateException("Tracking replay reset playback");
        for(var name:beforePose.keySet())if(!Arrays.equals(beforePose.get(name),afterPose.get(name)))throw new IllegalStateException("Tracking replay reset "+name);
        ClientProbe.log("PASS delayed same-sequence tracking packet action="+state.action());
    }
    public static void arm(UUID pet){interrupt=pet;passed=false;}
    public static void verify(){if(!passed)throw new IllegalStateException("Idle-to-pet priority packet never arrived");}
}
