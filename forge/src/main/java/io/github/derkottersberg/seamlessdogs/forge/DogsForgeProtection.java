package io.github.derkottersberg.seamlessdogs.forge;
import io.github.derkottersberg.seamlessdogs.gameplay.DigPermissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.core.BlockPos;
final class DogsForgeProtection {
    private static boolean loaded(String id) { return net.minecraftforge.fml.ModList.get().isLoaded(id); }
    static String status() { if(!NativeClaims.failureStatus.isEmpty())return NativeClaims.failureStatus;return "Protection: mobGriefing, spawn protection, native hooks. Claims: "+(loaded("ftbchunks")?"FTB Chunks; ":"")+(loaded("openpartiesandclaims")?"Open Parties and Claims; ":"")+"other claims need verified adapters."; }
    static boolean allowed(ServerPlayer owner,TamableAnimal pet,BlockPos pos,boolean commit) {
        if(!DigPermissions.vanilla(owner,pet,pos))return false;
        var level=(net.minecraft.server.level.ServerLevel)pet.level();
        if(loaded("ftbchunks")&&NativeClaims.ftbProtected(level,pos))return false;
        if(loaded("openpartiesandclaims") && NativeClaims.openPACProtected(level,pos))return false;
        return !commit || (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent((net.minecraft.server.level.ServerLevel)pet.level(),pet) && net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(pet,pos,pet.level().getBlockState(pos)) && net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(pet.level(),owner.gameMode.getGameModeForPlayer(),owner,pos)>=0);
    }
}
