package io.github.derkottersberg.seamlessdogs.client;
import com.google.gson.*;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExpressionsRigTest {
    private JsonArray frames(String name){return JsonParser.parseReader(new InputStreamReader(getClass().getResourceAsStream("/assets/seamlessdogs/animations/"+name+".json"))).getAsJsonObject().getAsJsonArray("frames");}
    private double value(JsonObject p,String bone,int i){return p.has(bone)?p.getAsJsonArray(bone).get(i).getAsDouble():0;}
    @Test void biscuitsAlternateAndStayAboveTheFloorOnBothNativeRigs(){
        for(boolean baby:new boolean[]{false,true}){
            var frames=frames("cat_knead"+(baby?"_baby":""));double length=baby?2:10;boolean left=false,right=false;
            for(int index=1;index<frames.size();index++){
                var a=frames.get(index-1).getAsJsonObject().getAsJsonObject("parts");var b=frames.get(index).getAsJsonObject().getAsJsonObject("parts");
                for(int step=0;step<=100;step++){
                    double t=step/100.;double[] paws=new double[2];int side=0;
                    for(String bone:new String[]{"left_front_leg","right_front_leg"}){
                        double angle=value(a,bone,0)*(1-t)+value(b,bone,0)*t,offset=value(a,bone,4)*(1-t)+value(b,bone,4)*t;
                        double r=Math.toRadians(angle);
                        paws[side++]=(baby?22:14.1)+offset+length*Math.cos(r)+(baby?Math.abs(Math.sin(r)):-2*Math.sin(r));
                        double top=(baby?22:14.1)+offset-(baby?Math.abs(Math.sin(r)):Math.max(0,2*Math.sin(r)));
                        // The adult shoulder must remain beneath the rolled chest's coat.
                        double bodyY=value(a,"body",4)*(1-t)+value(b,"body",4)*t;
                        double bodyRoll=Math.toRadians(value(a,"body",2)*(1-t)+value(b,"body",2)*t);
                        double backTop=(baby?19:14)+bodyY+2*Math.abs(Math.sin(bodyRoll));
                        assertTrue(top>=backTop-.02,"Front leg protrudes above the back");
                    }
                    double floor=baby?24:24.1;
                    assertTrue(paws[0]<=floor+.03&&paws[1]<=floor+.03,"Kneading paw penetrates the ground");
                    assertEquals(floor,Math.max(paws[0],paws[1]),.06,"Both paws leave the ground");
                    assertTrue(Math.min(paws[0],paws[1])>=floor-(baby?.17:.32),"Kneading lifts too high");
                    left|=paws[0]<paws[1]-.1;right|=paws[1]<paws[0]-.1;
                }
            }assertTrue(left&&right,"Biscuits must alternate paws");
        }
    }
    @Test void newClipsReturnToRestAndEarFoldsOnOneSide(){
        for(String name:new String[]{"cat_knead","cat_knead_baby","cat_groom_chest","cat_groom_chest_baby","dog_dig","dog_head_tilt"}){
            var f=frames(name);assertTrue(f.get(0).getAsJsonObject().getAsJsonObject("parts").isEmpty());assertTrue(f.get(f.size()-1).getAsJsonObject().getAsJsonObject("parts").isEmpty());
        }
        var held=frames("dog_head_tilt").get(5).getAsJsonObject().getAsJsonObject("parts");
        assertTrue(Math.abs(value(held,"head",2))>=18);assertTrue(Math.abs(value(held,"left_ear",2))>=35);assertTrue(Math.abs(value(held,"right_ear",2))<=5);
    }
    @Test void retiredPawWashesAreAbsentAndChestGroomingKeepsPawsGrounded() {
        for(String suffix:new String[]{"","_baby"}) {
            assertNull(getClass().getResource("/assets/seamlessdogs/animations/cat_groom"+suffix+".json"));
            assertNull(getClass().getResource("/assets/seamlessdogs/animations/cat_groom_left"+suffix+".json"));
            for(var frame:frames("cat_groom_chest"+suffix)) {
                var parts=frame.getAsJsonObject().getAsJsonObject("parts");
                assertFalse(parts.has("right_front_leg"));
                assertFalse(parts.has("left_front_leg"));
            }
        }
    }
    @Test void diggingHasAlternatingRecoveryAndGroundedScrapesAndRearSupport(){
        var f=frames("dog_dig");boolean left=false,right=false;double minZ=0,maxZ=0;
        for(int index=1;index<f.size();index++){
            var a=f.get(index-1).getAsJsonObject().getAsJsonObject("parts");var b=f.get(index).getAsJsonObject().getAsJsonObject("parts");
            for(int step=0;step<=20;step++){
                double t=step/20.;double[] paws=new double[2];int side=0;
                for(String bone:new String[]{"left_front_leg","right_front_leg","left_hind_leg","right_hind_leg"}){
                    double angle=Math.toRadians(value(a,bone,0)*(1-t)+value(b,bone,0)*t);
                    double y=16+value(a,bone,4)*(1-t)+value(b,bone,4)*t+8*Math.cos(angle)+Math.abs(Math.sin(angle));
                    if(bone.contains("hind"))assertEquals(24,y,.02,"Rear paw must support the dig");
                    else {assertTrue(y<=24.1,"Dig paw penetrates floor");paws[side++]=y;double z=8*Math.sin(angle);minZ=Math.min(minZ,z);maxZ=Math.max(maxZ,z);}
                }
                left|=paws[0]<paws[1]-.4;right|=paws[1]<paws[0]-.4;
            }
        }
        assertTrue(left&&right);assertTrue(maxZ-minZ>5,"Digging must reach and scrape, not swivel in place");
    }
}
