package de.melanx.skyblockbuilder.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;

public class SpawnBlock extends Block {

    public static final VoxelShape SHAPE = Block.makeCuboidShape(4, 4, 4, 12, 12, 12);

    public SpawnBlock() {
        super(AbstractBlock.Properties.from(Blocks.STRUCTURE_VOID));
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSpawnInBlock() {
        return true;
    }
}
