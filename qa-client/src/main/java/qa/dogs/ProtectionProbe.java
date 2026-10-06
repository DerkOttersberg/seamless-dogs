package qa.dogs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.EntityType;
/** Actual optional APIs in a disposable world; never shipped with Dogs. */
public final class ProtectionProbe {
    public static void run(ServerPlayer owner) {
        ServerLevel level=(ServerLevel)owner.level();
        BlockPos pos=new BlockPos(0,64,1);
        Wolf dog=new Wolf(EntityType.WOLF,level);dog.tame(owner);dog.setPos(.5,65,2);
        var platform=qa.dogs.mixin.WorldStateAccess.qa$platform();
        var manager=xaero.pac.common.server.api.OpenPACServerAPI.get(level.getServer()).getServerClaimsManager();
        var dimension=level.dimension().location();
        if(manager.get(dimension,pos)!=null)throw new IllegalStateException("Claim fixture was not empty");
        require(platform.mayDig(owner,dog,pos,false),"OpenPAC unclaimed terrain denied");
        try {
            require(manager.claim(dimension,owner.getUUID(),0,0,0,false)!=null,"OpenPAC own claim missing");
            require(!platform.mayDig(owner,dog,pos,false)&&!platform.mayDig(owner,dog,pos,true),"Dogs accepted its owner's claim");
            manager.unclaim(dimension,0,0);
            require(manager.claim(dimension,java.util.UUID.fromString("11111111-1111-4111-8111-111111111111"),0,0,0,false)!=null,"OpenPAC foreign claim missing");
            require(!platform.mayDig(owner,dog,pos,false)&&!platform.mayDig(owner,dog,pos,true),"Dogs accepted another owner's claim");
        } finally {manager.unclaim(dimension,0,0);}
        require(platform.mayDig(owner,dog,pos,false),"Unclaim did not restore allowed terrain");
        System.out.println("DOGS_OPENPAC_ACTUAL_CLAIMS_PASS owner and foreign claims denied during selection and commit; unclaimed allowed");
        if(Boolean.getBoolean("qa.ftb"))FtbProtectionProbe.run(owner,dog,pos);
        if(Boolean.getBoolean("qa.cpapi"))CommonProtectionProbe.run(owner,dog,pos);
        dog.discard();
    }
    static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
}
