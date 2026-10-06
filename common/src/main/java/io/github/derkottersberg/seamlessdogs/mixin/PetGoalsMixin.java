package io.github.derkottersberg.seamlessdogs.mixin;

import io.github.derkottersberg.seamlessdogs.gameplay.ActionGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Wolf.class, Cat.class})
public abstract class PetGoalsMixin {
    @Inject(method="registerGoals", at=@At("TAIL"))
    private void seamlessdogs$actions(CallbackInfo ci) {
        var pet = (TamableAnimal)(Object)this;
        var goals = ((MobGoalsAccess)pet).seamlessdogs$goals();
        goals.addGoal(1, new ActionGoal(pet, true));
        goals.addGoal(7, new ActionGoal(pet, false));
        goals.addGoal(1, new io.github.derkottersberg.seamlessdogs.gameplay.ReactionGoal(pet));
    }
}
