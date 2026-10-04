package io.github.derkottersberg.seamlessdogs.forge.gametest;
import io.github.derkottersberg.seamlessdogs.gametest.DogsScenarios;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.fml.common.Mod;
@Mod("seamlessdogstests")
@GameTestHolder("seamlessdogs")
@PrefixGameTestTemplate(false)
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
            channel.attr(io.netty.util.AttributeKey.<String>valueOf("fml:netversion"))
                .set(net.minecraftforge.network.NetworkConstants.NETVERSION);
            var player = new net.minecraft.server.level.ServerPlayer(level.getServer(), level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "Dogs-QA"));
            player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(level.getServer(), connection, player);
            var pos = helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1));
            player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            level.addNewPlayer(player);
            return player;
        });
    }
    public DogsNativeGameTests(){ }
    @GameTest(template="empty",timeoutTicks=80) public static void ownerCanPet(GameTestHelper helper){DogsScenarios.ownerCanPet(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void rejectInvalidRequests(GameTestHelper helper){DogsScenarios.rejectInvalidRequests(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void cooldownAndCancellation(GameTestHelper helper){DogsScenarios.cooldownAndCancellation(helper);}
    @GameTest(template="empty",timeoutTicks=80) public static void codecRoundTrip(GameTestHelper helper){DogsScenarios.codecRoundTrip(helper);}
}
