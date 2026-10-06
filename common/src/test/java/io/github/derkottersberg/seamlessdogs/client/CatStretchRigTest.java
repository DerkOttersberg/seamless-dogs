package io.github.derkottersberg.seamlessdogs.client;

import com.google.gson.*;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Regression for the actual vanilla leg lengths, rather than merely finite poses. */
class CatStretchRigTest {
    @Test void pawsStayGroundedAndReachForwardOnBothRigs() {
        for(boolean kitten:new boolean[]{false,true}) {
            String name=kitten?"cat_stretch_baby":"cat_stretch";
            var input=getClass().getResourceAsStream("/assets/seamlessdogs/animations/"+name+".json");
            assertNotNull(input);
            var frames=JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject().getAsJsonArray("frames");
            double length=kitten?2:10;
            for(int index=1;index<frames.size();index++) {
                var a=frames.get(index-1).getAsJsonObject().getAsJsonObject("parts");
                var b=frames.get(index).getAsJsonObject().getAsJsonObject("parts");
                for(int step=0;step<=100;step++) {
                    double fraction=step/100.0;
                    double angle=value(a,"left_front_leg",0)*(1-fraction)+value(b,"left_front_leg",0)*fraction;
                    double offset=value(a,"left_front_leg",4)*(1-fraction)+value(b,"left_front_leg",4)*fraction;
                    double pawHeight=offset+length*Math.cos(Math.toRadians(angle));
                    assertEquals(length,pawHeight,.65,"Paw penetrates/leaves ground: "+name);
                }
            }
            var held=frames.get(3).getAsJsonObject().getAsJsonObject("parts");
            assertTrue(value(held,"body",0)>0,"Back must lift instead of collapsing downward");
            double forward=length*Math.sin(Math.toRadians(value(held,"left_front_leg",0)))+value(held,"left_front_leg",5);
            assertTrue(forward<-(kitten?1:6),"Front paws must visibly reach forward");
            assertTrue(frames.get(0).getAsJsonObject().getAsJsonObject("parts").isEmpty());
            assertTrue(frames.get(frames.size()-1).getAsJsonObject().getAsJsonObject("parts").isEmpty());
        }
    }
    @Test void adultNeckRemainsAttachedAndHindPawsShiftWithoutFloating() {
        var input=getClass().getResourceAsStream("/assets/seamlessdogs/animations/cat_stretch.json");
        assertNotNull(input);
        var frames=JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject().getAsJsonArray("frames");
        double minPaw=Double.POSITIVE_INFINITY,maxPaw=Double.NEGATIVE_INFINITY;
        for(int index=1;index<frames.size();index++) {
            var a=frames.get(index-1).getAsJsonObject().getAsJsonObject("parts");
            var b=frames.get(index).getAsJsonObject().getAsJsonObject("parts");
            for(int step=0;step<=100;step++) {
                double t=step/100.0;
                double[] head=interpolate(a,b,"head",t),body=interpolate(a,b,"body",t);
                double headRear=maximumZ(head,0,15,-9,0,new double[]{-2.5,-2,-3},new double[]{2.5,2,2});
                double bodyFront=minimumZ(body,0,12,-10,90,new double[]{-2,3,-8},new double[]{2,19,-2});
                assertTrue(headRear>=bodyFront-.25,"Adult head detaches from torso");
                for(String bone:new String[]{"left_hind_leg","right_hind_leg"}) {
                    double[] leg=interpolate(a,b,bone,t);
                    double angle=Math.toRadians(leg[0]);
                    assertEquals(24,18+leg[4]+6*Math.cos(angle)-2*Math.sin(angle),.2,"Adult hind paw floats");
                    double paw=5+leg[5]+6*Math.sin(angle)+2*Math.cos(angle);
                    if(index>=3&&index<=6){minPaw=Math.min(minPaw,paw);maxPaw=Math.max(maxPaw,paw);}
                }
            }
        }
        assertTrue(maxPaw-minPaw>.25,"Adult hind paws remain frozen during the hold");
    }
    private static double[] interpolate(JsonObject a,JsonObject b,String bone,double t) {
        double[] pose=new double[6];for(int i=0;i<6;i++)pose[i]=value(a,bone,i)*(1-t)+value(b,bone,i)*t;return pose;
    }
    private static double maximumZ(double[] p,double x,double y,double z,double restX,double[] low,double[] high) {
        return zBounds(p,x,y,z,restX,low,high)[1];
    }
    private static double minimumZ(double[] p,double x,double y,double z,double restX,double[] low,double[] high) {
        return zBounds(p,x,y,z,restX,low,high)[0];
    }
    private static double[] zBounds(double[] p,double x,double y,double z,double restX,double[] low,double[] high) {
        double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
        for(int i=0;i<8;i++) {
            var point=new org.joml.Vector3d((i&1)==0?low[0]:high[0],(i&2)==0?low[1]:high[1],(i&4)==0?low[2]:high[2]);
            point.rotateX(Math.toRadians(restX+p[0])).rotateY(Math.toRadians(p[1])).rotateZ(Math.toRadians(p[2])).add(x+p[3],y+p[4],z+p[5]);
            min=Math.min(min,point.z);max=Math.max(max,point.z);
        }
        return new double[]{min,max};
    }
    private static double value(JsonObject parts,String bone,int component) {
        return parts.has(bone)?parts.getAsJsonArray(bone).get(component).getAsDouble():0;
    }
}
