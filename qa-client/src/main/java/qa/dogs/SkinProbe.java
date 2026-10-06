package qa.dogs;
import java.util.*;
import net.minecraft.client.resources.DefaultPlayerSkin;
/** Test-only native skin/model selection; packet sequence keeps both clients consistent. */
public final class SkinProbe {
    public static boolean slim;
    public static int playerPoseChecks;
    private static final Set<String> rendered=new HashSet<>();
    public static void receive(io.github.derkottersberg.seamlessdogs.network.PetUpdate state){
        if(state.action()==1||state.action()==2)slim=(state.sequence()&1)==0;
    }
    public static void record(String model){rendered.add(model.toLowerCase(java.util.Locale.ROOT));}
    public static void verify(){
        if(!rendered.contains("slim")||!(rendered.contains("wide")||rendered.contains("default")))throw new IllegalStateException("Both animated player models were not rendered: "+rendered);
        if(playerPoseChecks<8)throw new IllegalStateException("Too few native cat-petting player pose checks: "+playerPoseChecks);
        ClientProbe.log("DOGS_PLAYER_POSE_PASS bounded native head/body/arms across "+playerPoseChecks+" animated draws");
        ClientProbe.log("DOGS_SKIN_MODELS_PASS "+rendered);
    }
    public static net.minecraft.client.resources.PlayerSkin skin(){
        var desired=slim?net.minecraft.client.resources.PlayerSkin.Model.SLIM:net.minecraft.client.resources.PlayerSkin.Model.WIDE;
        for(int i=0;i<100;i++){var skin=DefaultPlayerSkin.get(new UUID(0,i));if(skin.model()==desired)return skin;}
        throw new IllegalStateException("Vanilla skin model unavailable");
    }

}
