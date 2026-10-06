package io.github.derkottersberg.seamlessdogs.client;

/** Pixel-only eyelid authoring, shared with tests without loading the renderer. */
public final class CatEyelids {
    private CatEyelids() { }
    public static int lidColor(int fur) {
        int r=(fur>>>16)&255,g=(fur>>>8)&255,b=fur&255;
        // A muted warm highlight on dark coats, a dark crease on light coats.
        return (.2126*r+.7152*g+.0722*b)<85 ? 0xFF9B9298 : 0xFF302B29;
    }
    public static boolean crease(boolean baby,boolean smile,int x,int y) {
        if(baby)return y==5&&(x==5||x==7);
        if(!smile)return y==6&&(x==5||x==6||x==8||x==9);
        return (y==6&&(x==6||x==8))||(y==7&&(x==5||x==9));
    }
    public static boolean supported(int width,int height,boolean baby) {
        int base=baby?32:64;
        return width>=base&&width%base==0&&height*base==width*32;
    }
}
