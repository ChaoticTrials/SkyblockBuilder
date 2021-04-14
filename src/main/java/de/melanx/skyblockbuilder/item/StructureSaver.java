package de.melanx.skyblockbuilder.item;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.event.RegistryEvent;
import org.apache.commons.compress.utils.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class StructureSaver extends Item {
    public StructureSaver() {
        super(new Properties().group(ItemGroup.TOOLS));
        this.setRegistryName(SkyblockBuilder.MODID, "structure_saver");
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
                CompoundNBT position = new CompoundNBT();
                RandomUtility.writeBlockPos(pos, position);

                tag.put("Position1", position);
                player.sendStatusMessage(new StringTextComponent("Position 1 set at " + pos), false);
                return ActionResultType.SUCCESS;
            }

            if (!tag.contains("Position2")) {
                CompoundNBT position = new CompoundNBT();
                RandomUtility.writeBlockPos(pos, position);

                tag.put("Position2", position);
                player.sendStatusMessage(new StringTextComponent("Position 2 set at " + pos), false);
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

        if (!world.isRemote && tag.contains("Position1") && tag.contains("Position2")) {

            // prevent instant save
            if (!tag.contains("CanSave")) {
                tag.putBoolean("CanSave", true);
                return ActionResult.resultPass(stack);
            }

            String schematic = saveSchematic(world, stack);
            if (schematic == null) {
                player.sendStatusMessage(new StringTextComponent("Something went terribly wrong."), false);
                return ActionResult.resultPass(stack);
            }

            tag.remove("Position1");
            tag.remove("Position2");
            tag.remove("CanSave");
            player.sendStatusMessage(new StringTextComponent("Successfully saved structure as " + schematic), false);

            return ActionResult.func_233538_a_(stack, false);
        }

        return ActionResult.resultPass(stack);
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote) {
            MutableBoundingBox area = getArea(stack);
        }
    }

    @Nullable
    public static MutableBoundingBox getArea(ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTag();
        if (!nbt.contains("Position1") || !nbt.contains("Position2")) {
            return null;
        }

        CompoundNBT tag1 = nbt.getCompound("Position1");
        CompoundNBT tag2 = nbt.getCompound("Position2");

        BlockPos pos1 = RandomUtility.getPosFromNbt(tag1);
        BlockPos pos2 = RandomUtility.getPosFromNbt(tag2);

        return new MutableBoundingBox(pos1, pos2);
    }

    public static void register(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new StructureSaver());
    }

    public static String saveSchematic(World world, ItemStack stack) {
        return saveSchematic(world, stack, null);
    }

    public static String saveSchematic(World world, ItemStack stack, String name) {
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
            SkyblockBuilder.LOGGER.warn("Could not create folder: {}", folderPath);
            return null;
        }

        int index = 0;
        String filename;
        String filepath;
        do {
            filename = (name == null ? "template" : name) + ((index == 0) ? "" : "_" + index) + ".nbt";
            index++;
            filepath = folderPath + "/" + filename;
        } while (Files.exists(Paths.get(filepath)));

        Path path = Paths.get(filepath);
        OutputStream outputStream = null;
        try {
            outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE);
            CompoundNBT nbttagcompound = template.writeToNBT(new CompoundNBT());
            CompressedStreamTools.writeCompressed(nbttagcompound, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (outputStream != null)
                IOUtils.closeQuietly(outputStream);
        }

        return filename;
    }
}
