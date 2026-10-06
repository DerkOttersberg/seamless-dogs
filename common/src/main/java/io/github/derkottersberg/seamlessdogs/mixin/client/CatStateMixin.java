package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.*;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(CatRenderState.class)
public abstract class CatStateMixin implements ActionRenderData {
    @Unique private AnimationClips.Pose seamlessdogs$pose = AnimationClips.Pose.NONE;
    public AnimationClips.Pose seamlessdogs$actionPose() { return seamlessdogs$pose; }
    public void seamlessdogs$actionPose(AnimationClips.Pose pose) { seamlessdogs$pose=pose; }
}
