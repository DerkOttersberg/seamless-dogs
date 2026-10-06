package io.github.derkottersberg.seamlessdogs.gameplay;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Blocks;

public final class DigSite {
    public static final TagKey<Block> DIGGABLE = TagKey.create(Registries.BLOCK, SeamlessDogs.id("diggable"));
    private DigSite() { }
    public static boolean eligible(ServerLevel level, BlockPos pos) {
        // Inspect a loaded 3x3x3 neighborhood before touching any state.
        for (BlockPos neighbor : BlockPos.betweenClosed(pos.offset(-1,-1,-1), pos.offset(1,1,1)))
            if (!level.hasChunkAt(neighbor) || !level.getWorldBorder().isWithinBounds(neighbor)) return false;
        var state = level.getBlockState(pos);
        if (!state.is(DIGGABLE) || state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT_PATH) || state.hasBlockEntity() || !state.getFluidState().isEmpty()
            || !state.isCollisionShapeFullBlock(level, pos) || !level.getBlockState(pos.above()).isAir()
            || !level.getBlockState(pos.below()).isCollisionShapeFullBlock(level, pos.below())) return false;
        for (Direction direction : Direction.values()) {
            var neighbor = level.getBlockState(pos.relative(direction));
            if (!neighbor.getFluidState().isEmpty() || neighbor.hasBlockEntity()) return false;
            // Face-attached plants, torches and other partial structures can depend on this block.
            if(direction.getAxis().isHorizontal()&&!neighbor.isAir()
                && !neighbor.isCollisionShapeFullBlock(level,pos.relative(direction)))return false;
            // Sand at the same height would collapse into the new opening if unsupported.
            if (neighbor.getBlock() instanceof FallingBlock
                && !level.getBlockState(pos.relative(direction).below()).isCollisionShapeFullBlock(level, pos.relative(direction).below())) return false;
        }
        return true;
    }
}
