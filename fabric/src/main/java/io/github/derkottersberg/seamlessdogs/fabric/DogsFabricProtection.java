package io.github.derkottersberg.seamlessdogs.fabric;
import io.github.derkottersberg.seamlessdogs.gameplay.DigPermissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.core.BlockPos;
final class DogsFabricProtection {
    private static boolean loaded(String id) { return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(id); }
    static String status() { if(!NativeClaims.failureStatus.isEmpty())return NativeClaims.failureStatus;return loaded("ftbchunks")?"Digging paused: FTB Chunks has no validated build for this line.":"Protection: mobGriefing, spawn protection, native hooks. Claims: "+(loaded("openpartiesandclaims")?"Open Parties and Claims; ":"")+(loaded("common-protection-api")?"Fabric Common Protection API; ":"")+"other claims need verified adapters."; }
    static boolean allowed(ServerPlayer owner,TamableAnimal pet,BlockPos pos) {
        if(loaded("ftbchunks") || !DigPermissions.vanilla(owner,pet,pos))return false;
        var level=(net.minecraft.server.level.ServerLevel)pet.level();
        if(loaded("openpartiesandclaims") && NativeClaims.openPACProtected(level,pos))return false;
        if(loaded("common-protection-api") && NativeClaims.commonProtected(level,pos))return false;
        return true;
    }
}
