package de.melanx.skyblockbuilder.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable IBlockReader world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".desc").mergeStyle(TextFormatting.AQUA));
    }
}
