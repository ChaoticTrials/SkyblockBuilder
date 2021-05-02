package de.melanx.skyblockbuilder.registration;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.RandomUtility;
import io.github.noeppi_noeppi.libx.util.NBTX;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.ClientPlayerEntity;
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

public class ItemStructureSaver extends Item {
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
                ((ClientPlayerEntity) player).mc.displayGuiScreen(new ScreenStructureSaver(stack, new TranslationTextComponent("screen.skyblockbuilder.structure_saver")));
            }

            return ActionResult.func_233538_a_(stack, false);
        }

        return ActionResult.resultPass(stack);
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

        String folderPath = "skyblock_exports";
        try {
            Files.createDirectories(Paths.get(folderPath));
        } catch (IOException e) {
            SkyblockBuilder.getLogger().warn("Could not create folder: {}", folderPath);
            return null;
        }

        Path path = Paths.get(RandomUtility.getFilePath(folderPath, name));
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
