package io.github.derkottersberg.seamlessdogs.mixin.client;
import io.github.derkottersberg.seamlessdogs.client.DogRenderData;
import io.github.derkottersberg.seamlessdogs.client.PetAnimation;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(WolfRenderState.class)
public abstract class WolfStateMixin implements DogRenderData, io.github.derkottersberg.seamlessdogs.client.ActionRenderData {
    @Unique private PetAnimation.Sample seamlessdogs$pose = PetAnimation.sample(-1);
    public PetAnimation.Sample seamlessdogs$sample() { return seamlessdogs$pose; }
    public void seamlessdogs$sample(PetAnimation.Sample sample) { seamlessdogs$pose = sample; }
    @Unique private io.github.derkottersberg.seamlessdogs.client.AnimationClips.Pose seamlessdogs$action = io.github.derkottersberg.seamlessdogs.client.AnimationClips.Pose.NONE;
    public io.github.derkottersberg.seamlessdogs.client.AnimationClips.Pose seamlessdogs$actionPose() { return seamlessdogs$action; }
    public void seamlessdogs$actionPose(io.github.derkottersberg.seamlessdogs.client.AnimationClips.Pose pose) { seamlessdogs$action=pose; }
}
