package io.github.derkottersberg.seamlessdogs.client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CatEyelidsTest {
    @Test void everyClosedExpressionHasTwoVisibleEyesOnBothRigs() {
        for(boolean baby:new boolean[]{false,true})for(boolean smile:new boolean[]{false,true}) {
            int left=0,right=0;
            for(int x=4;x<=10;x++)for(int y=5;y<=7;y++)if(CatEyelids.crease(baby,smile,x,y)) {
                if(x<7)left++;else right++;
            }
            assertTrue(left>0&&right>0,"Blink must draw both eyelids");
        }
    }
    @Test void creaseContrastsWithBlackAndWhiteFurAndStaysOpaque() {
        for(int fur:new int[]{0xFF161524,0xFF1C1827,0xFFE0DDD9,0xFF957256}) {
            int lid=CatEyelids.lidColor(fur);
            assertEquals(255,lid>>>24);assertTrue(Math.abs(luma(lid)-luma(fur))>40);
        }
    }
    @Test void customLayoutsFallBackWhileIntegerScaledVanillaLayoutsAreSupported() {
        assertTrue(CatEyelids.supported(128,64,false));assertTrue(CatEyelids.supported(64,64,true));
        assertFalse(CatEyelids.supported(128,128,false));assertFalse(CatEyelids.supported(16,16,true));
    }
    private double luma(int argb){return .2126*((argb>>>16)&255)+.7152*((argb>>>8)&255)+.0722*(argb&255);}
}
