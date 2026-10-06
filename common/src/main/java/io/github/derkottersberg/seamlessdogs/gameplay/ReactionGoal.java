package io.github.derkottersberg.seamlessdogs.gameplay;
import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import java.util.EnumSet;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;

/** LOOK only, so a seated wolf can look back without competing with its sit goal. */
public final class ReactionGoal extends Goal {
    private final TamableAnimal pet;
    public ReactionGoal(TamableAnimal pet){this.pet=pet;setFlags(EnumSet.of(Flag.LOOK));}
    public boolean canUse(){return pet.getTarget()==null&&pet.hurtTime==0&&SeamlessDogs.reactionActive(pet);}
    public boolean canContinueToUse(){return canUse();}
    public void tick(){SeamlessDogs.lookAtOwner(pet);}
}
