package qa.dogs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.TamableAnimal;
final class FtbProtectionProbe {
    static void run(ServerPlayer owner,TamableAnimal dog,BlockPos pos) {
        var api=dev.ftb.mods.ftbchunks.api.FTBChunksAPI.api();
        ProtectionProbe.require(api.isManagerLoaded(),"FTB manager missing");
        var manager=api.getManager();var data=manager.getOrCreateData(owner);
        var chunk=new dev.ftb.mods.ftblibrary.math.ChunkDimPos((net.minecraft.server.level.ServerLevel)owner.level(),pos);
        var platform=qa.dogs.mixin.WorldStateAccess.qa$platform();
        ProtectionProbe.require(manager.getChunk(chunk)==null&&platform.mayDig(owner,dog,pos,false),"FTB initial terrain denied");
        try {
            data.claim(owner.createCommandSourceStack(),chunk,false);
            ProtectionProbe.require(manager.getChunk(chunk)!=null,"FTB real claim was not created");
            manager.setBypassProtection(owner.getUUID(),true);
            ProtectionProbe.require(!platform.mayDig(owner,dog,pos,false)&&!platform.mayDig(owner,dog,pos,true),"Dogs accepted an owner/admin-bypassed FTB claim");
        } finally {manager.setBypassProtection(owner.getUUID(),false);data.unclaim(owner.createCommandSourceStack(),chunk,false);}
        ProtectionProbe.require(manager.getChunk(chunk)==null&&platform.mayDig(owner,dog,pos,false),"FTB unclaim did not restore terrain");
        System.out.println("DOGS_FTB_ACTUAL_CLAIMS_PASS owner's real FTB claim denied during selection and commit despite administrator bypass; unclaimed allowed");
    }
}
