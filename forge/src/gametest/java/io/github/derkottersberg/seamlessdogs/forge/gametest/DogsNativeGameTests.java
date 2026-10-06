package io.github.derkottersberg.seamlessdogs.forge.gametest;
import io.github.derkottersberg.seamlessdogs.gametest.DogsScenarios;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.GameTestPrefix;
import net.minecraftforge.fml.common.Mod;
@Mod("seamlessdogstests")
@GameTestHolder(value="seamlessdogstests", namespace="seamlessdogs")
@GameTestPrefix("seamlessdogs")
public final class DogsNativeGameTests {
    static {
        // Forge's vanilla mock has no channel. Attach an in-process packet
        // connection so production permission and recipient checks remain active.
        DogsScenarios.usePlayerFactory(helper -> {
            var level = helper.getLevel();
            var channel = new io.netty.channel.embedded.EmbeddedChannel();
            var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
            channel.pipeline().addLast("packet_handler", connection);
            channel.pipeline().fireChannelActive();
            var profile = new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "Dogs-QA");
            var player = new net.minecraft.server.level.ServerPlayer(level.getServer(), level,
                profile, net.minecraft.server.level.ClientInformation.createDefault());
            player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(level.getServer(), connection, player,
                net.minecraft.server.network.CommonListenerCookie.createInitial(profile, false));
            var pos = helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1));
            player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            level.getServer().getPlayerList().placeNewPlayer(connection,player,net.minecraft.server.network.CommonListenerCookie.createInitial(profile,false));
            return player;
        });
    }
    public DogsNativeGameTests(){ }
    @GameTest(template="seamlessdogs:empty",timeoutTicks=80) public static void ownerCanPet(GameTestHelper helper){DogsScenarios.ownerCanPet(helper);}
    @GameTest(template="seamlessdogs:empty",timeoutTicks=80) public static void rejectInvalidRequests(GameTestHelper helper){DogsScenarios.rejectInvalidRequests(helper);}
    @GameTest(template="seamlessdogs:empty",timeoutTicks=80) public static void cooldownAndCancellation(GameTestHelper helper){DogsScenarios.cooldownAndCancellation(helper);}
    @GameTest(template="seamlessdogs:empty",timeoutTicks=80) public static void codecRoundTrip(GameTestHelper helper){DogsScenarios.codecRoundTrip(helper);}
    @GameTest(template="seamlessdogs:empty",timeoutTicks=80) public static void catOwnership(GameTestHelper helper){DogsScenarios.catOwnership(helper);}
    @GameTest(template="seamlessdogs:empty",timeoutTicks=80) public static void diggingTerrain(GameTestHelper helper){DogsScenarios.diggingTerrain(helper);}
    @GameTest(template="seamlessdogs:empty",timeoutTicks=80) public static void v2CodecRoundTrip(GameTestHelper helper){DogsScenarios.v2CodecRoundTrip(helper);}
    @GameTest(template="seamlessdogs:empty",timeoutTicks=270) public static void idleContracts(GameTestHelper helper){DogsScenarios.idleContracts(helper);}
}
