package io.github.derkottersberg.seamlessdogs.gameplay;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import java.util.EnumSet;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;

/** Only owns movement during an accepted action; never changes sitting, taming or combat goals. */
public final class ActionGoal extends Goal {
    private final TamableAnimal pet;
    private final boolean petting;
    public ActionGoal(TamableAnimal pet, boolean petting) {
        this.pet = pet; this.petting = petting;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    public boolean canUse() { return pet.getTarget() == null && pet.hurtTime == 0 && !SeamlessDogs.reactionActive(pet) && SeamlessDogs.actionActive(pet, petting); }
    public boolean canContinueToUse() { return canUse(); }
    public void start() { pet.getNavigation().stop(); }
    public void tick() { pet.getNavigation().stop(); }
}
