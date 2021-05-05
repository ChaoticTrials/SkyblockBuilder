package de.melanx.skyblockbuilder.item;

import de.melanx.skyblockbuilder.util.ClientUtility;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyPaths;
import io.github.noeppi_noeppi.libx.util.NBTX;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import org.apache.commons.compress.utils.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ItemStructureSaver extends Item {

    private static final IFormattableTextComponent TOOLTIP_INFO = new TranslationTextComponent("skyblockbuilder.item.structure_saver.info.tooltip").mergeStyle(TextFormatting.GOLD);

    public ItemStructureSaver() {
        super(new Properties().group(ItemGroup.TOOLS));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(@Nonnull ItemUseContext context) {
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();

        if (!context.getWorld().isRemote && player != null && player.isSneaking()) {
            ItemStack stack = context.getItem();
            CompoundNBT tag = stack.getOrCreateTag();

            if (!tag.contains("Position1")) {
                NBTX.putPos(tag, "Position1", pos);
                player.sendStatusMessage(new TranslationTextComponent("skyblockbuilder.structure_saver.pos", 1, pos.getX(), pos.getY(), pos.getZ()), false);
                return ActionResultType.SUCCESS;
            }

            if (!tag.contains("Position2")) {
                NBTX.putPos(tag, "Position2", pos);
                player.sendStatusMessage(new TranslationTextComponent("skyblockbuilder.structure_saver.pos", 2, pos.getX(), pos.getY(), pos.getZ()), false);
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        CompoundNBT tag = stack.getOrCreateTag();

        if (tag.contains("Position1") && tag.contains("Position2")) {

            // prevent instant save
            if (!tag.contains("CanSave")) {
                tag.putBoolean("CanSave", true);
                return ActionResult.resultPass(stack);
            }

            if (world.isRemote) {
                ClientUtility.openItemScreen(stack);
            }

            return ActionResult.func_233538_a_(stack, false);
        }

        return ActionResult.resultPass(stack);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        CompoundNBT nbt = stack.getOrCreateTag();
        BlockPos pos1 = NBTX.getPos(nbt, "Position1");
        BlockPos pos2 = NBTX.getPos(nbt, "Position2");

        if (pos1 != null) {
            tooltip.add(new TranslationTextComponent("skyblockbuilder.item.structure_saver.position.tooltip", 1, pos1.getX(), pos1.getY(), pos1.getZ()).mergeStyle(TextFormatting.DARK_GRAY));
        }

        if (pos2 != null) {
            tooltip.add(new TranslationTextComponent("skyblockbuilder.item.structure_saver.position.tooltip", 2, pos2.getX(), pos2.getY(), pos2.getZ()).mergeStyle(TextFormatting.DARK_GRAY));
        }

        tooltip.add(TOOLTIP_INFO);
    }

    @Nullable
    public static MutableBoundingBox getArea(ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTag();
        if (!nbt.contains("Position1") || !nbt.contains("Position2")) {
            return null;
        }

        BlockPos pos1 = NBTX.getPos(nbt, "Position1");
        BlockPos pos2 = NBTX.getPos(nbt, "Position2");

        //noinspection ConstantConditions
        return new MutableBoundingBox(pos1, pos2);
    }

    public static String saveSchematic(World world, ItemStack stack) {
        return saveSchematic(world, stack, null);
    }

    public static String saveSchematic(World world, ItemStack stack, @Nullable String name) {
        Template template = new Template();
        MutableBoundingBox boundingBox = getArea(stack);

        if (boundingBox == null) {
            return null;
        }

        BlockPos origin = new BlockPos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        BlockPos bounds = new BlockPos(boundingBox.getXSize(), boundingBox.getYSize(), boundingBox.getZSize());

        template.takeBlocksFromWorld(world, origin, bounds, true, Blocks.STRUCTURE_VOID);

        Path path = Paths.get(RandomUtility.getFilePath(SkyPaths.MOD_EXPORTS.getFileName().toString(), name));
        OutputStream outputStream = null;
        try {
            outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE);
            CompoundNBT nbttagcompound = template.writeToNBT(new CompoundNBT());
            CompressedStreamTools.writeCompressed(nbttagcompound, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (outputStream != null) {
                IOUtils.closeQuietly(outputStream);
            }
        }

        return path.getFileName().toString();
    }

    public static ItemStack removeTags(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        tag.remove("Position1");
        tag.remove("Position2");
        tag.remove("CanSave");
        stack.setTag(tag);
        return stack;
    }
}
