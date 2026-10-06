package io.github.derkottersberg.seamlessdogs.gameplay;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.gamerules.GameRules;
public final class DigPermissions {
    private DigPermissions() { }
    public static boolean vanilla(ServerPlayer owner,TamableAnimal pet,BlockPos pos) {
        if (!(pet.level() instanceof ServerLevel level) || owner.level()!=level || !level.getGameRules().get(GameRules.MOB_GRIEFING)) return false;
        return !owner.isSpectator() && !owner.blockActionRestricted(level,pos,owner.gameMode.getGameModeForPlayer())
            && !level.getServer().isUnderSpawnProtection(level,pos,owner);
    }
}
