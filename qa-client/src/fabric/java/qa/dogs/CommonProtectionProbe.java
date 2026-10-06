package qa.dogs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
final class CommonProtectionProbe {
    static void run(ServerPlayer owner,TamableAnimal dog,BlockPos pos) {
        var platform=qa.dogs.mixin.WorldStateAccess.qa$platform();
        var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("seamlessdogsqa","test_claim");
        ProtectionProbe.require(platform.mayDig(owner,dog,pos,false),"CPAPI initial terrain denied");
        eu.pb4.common.protection.api.CommonProtection.register(id,new eu.pb4.common.protection.api.ProtectionProvider(){
            public boolean isProtected(Level level,BlockPos block){return level==owner.level()&&pos.equals(block);}
            public boolean isAreaProtected(Level level,AABB bounds){return level==owner.level()&&bounds.contains(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);}
        });
        try {ProtectionProbe.require(!platform.mayDig(owner,dog,pos,false)&&!platform.mayDig(owner,dog,pos,true),"Dogs ignored registered CPAPI protection");}
        finally {eu.pb4.common.protection.api.CommonProtection.remove(id);}
        ProtectionProbe.require(platform.mayDig(owner,dog,pos,false),"CPAPI unregister did not restore terrain");
        System.out.println("DOGS_CPAPI_ACTUAL_PROVIDER_PASS actual public provider denies selection and commit; unregister restores unclaimed terrain");
    }
}
