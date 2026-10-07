package qa.dogs.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

/** Starts only a private QA profile after Forge completes its loading overlay. */
final class DogsQaStartup {
    private static int titleTicks;
    private static boolean started;
    static boolean tick(Minecraft client) {
        String world=System.getProperty("qa.openWorld");
        String address=System.getProperty("qa.connectAddress");
        if(started || world==null && address==null)return true;
        if(client.getOverlay()!=null || !(client.screen instanceof TitleScreen))return false;
        if(++titleTicks<20)return false;
        started=true;
        if(address!=null) {
            System.out.println("DOGS_QA_POST_LOAD_CONNECT");
            ConnectScreen.startConnecting(client.screen,client,ServerAddress.parseString(address),new ServerData("Private Dogs QA",address,ServerData.Type.OTHER),false,null);
        } else {
            System.out.println("DOGS_QA_POST_LOAD_OPEN");
            client.createWorldOpenFlows().openWorld(world,()->client.setScreen(new TitleScreen()));
        }
        return false;
    }
}
