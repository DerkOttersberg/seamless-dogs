package io.github.derkottersberg.seamlessdogs.neoforge;
import io.github.derkottersberg.seamlessdogs.gameplay.DigPermissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.core.BlockPos;
final class DogsNeoForgeProtection {
    private static boolean loaded(String id) { return net.neoforged.fml.ModList.get().isLoaded(id); }
    static String status() { if(!NativeClaims.failureStatus.isEmpty())return NativeClaims.failureStatus;return loaded("ftbchunks")?"Digging paused: FTB Chunks has no validated build for this line.":"Protection: mobGriefing, spawn protection, native hooks. Claims: "+(loaded("openpartiesandclaims")?"Open Parties and Claims; ":"")+"other claims need verified adapters."; }
    static boolean allowed(ServerPlayer owner,TamableAnimal pet,BlockPos pos,boolean commit) {
        if(loaded("ftbchunks") || !DigPermissions.vanilla(owner,pet,pos))return false;
        var level=(net.minecraft.server.level.ServerLevel)pet.level();
        if(loaded("openpartiesandclaims") && NativeClaims.openPACProtected(level,pos))return false;
        return !commit || (net.neoforged.neoforge.event.EventHooks.canEntityGrief((net.minecraft.server.level.ServerLevel)pet.level(),pet) && net.neoforged.neoforge.event.EventHooks.onEntityDestroyBlock(pet,pos,pet.level().getBlockState(pos)) && !net.neoforged.neoforge.common.CommonHooks.fireBlockBreak(pet.level(),owner.gameMode.getGameModeForPlayer(),owner,pos,pet.level().getBlockState(pos)).isCanceled());
    }
}
