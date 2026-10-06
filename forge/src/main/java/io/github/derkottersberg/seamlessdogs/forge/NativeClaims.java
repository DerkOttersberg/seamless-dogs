package io.github.derkottersberg.seamlessdogs.forge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
/** Loaded only when the corresponding optional mod is present. Public APIs, no reflective discovery. */
final class NativeClaims {
    static String failureStatus="";
    private static void fail(Throwable failure){if(failureStatus.isEmpty()){failureStatus="Digging disabled: installed protection API could not be queried safely.";org.slf4j.LoggerFactory.getLogger("SeamlessDogs").warn(failureStatus,failure);}}
    static boolean openPACProtected(ServerLevel level,BlockPos pos) {
        if(!failureStatus.isEmpty())return true;
        try { return xaero.pac.common.server.api.OpenPACServerAPI.get(level.getServer()).getServerClaimsManager().get(level.dimension().location(),pos)!=null; }
        catch(LinkageError | RuntimeException failure) { fail(failure);return true; }
    }
    static boolean ftbProtected(ServerLevel level,BlockPos pos){
        if(!failureStatus.isEmpty())return true;
        try{var api=dev.ftb.mods.ftbchunks.api.FTBChunksAPI.api();
            if(!api.isManagerLoaded())throw new IllegalStateException("FTB claim manager is not ready");
            return api.getManager().getChunk(new dev.ftb.mods.ftblibrary.math.ChunkDimPos(level,pos))!=null;
        }catch(LinkageError|RuntimeException failure){fail(failure);return true;}
    }
}
