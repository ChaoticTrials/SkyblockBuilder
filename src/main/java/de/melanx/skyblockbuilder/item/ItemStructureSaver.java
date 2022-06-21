package de.melanx.skyblockbuilder.item;

import com.google.common.collect.Sets;
import de.melanx.skyblockbuilder.util.ClientUtility;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;

public class ItemStructureSaver extends Item {

    private static final MutableComponent TOOLTIP_INFO = Component.translatable("skyblockbuilder.item.structure_saver.info.tooltip").withStyle(ChatFormatting.GOLD);
    private static final MutableComponent TOOLTIP_SAVE = Component.translatable("skyblockbuilder.item.structure_saver.save.tooltip").withStyle(ChatFormatting.GOLD);

    public ItemStructureSaver() {
        super(new Properties().tab(CreativeModeTab.TAB_TOOLS));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (!context.getLevel().isClientSide && player != null && player.isShiftKeyDown()) {
            ItemStack stack = context.getItemInHand();
            CompoundTag tag = stack.getOrCreateTag();

            if (!tag.contains("Position1")) {
                tag.put("Position1", NbtUtils.writeBlockPos(pos));
                player.displayClientMessage(Component.translatable("skyblockbuilder.structure_saver.pos", 1, pos.getX(), pos.getY(), pos.getZ()), false);
                return InteractionResult.SUCCESS;
            }

            if (!tag.contains("Position2")) {
                tag.put("Position2", NbtUtils.writeBlockPos(pos));
                player.displayClientMessage(Component.translatable("skyblockbuilder.structure_saver.pos", 2, pos.getX(), pos.getY(), pos.getZ()), false);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();

        if (tag.contains("Position1") && tag.contains("Position2")) {

            // prevent instant save
            if (!tag.contains("CanSave")) {
                tag.putBoolean("CanSave", true);
                return InteractionResultHolder.pass(stack);
            }

            if (level.isClientSide) {
                ClientUtility.openItemScreen(stack);
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag nbt = stack.getOrCreateTag();

        if (nbt.contains("Position1")) {
            BlockPos pos = NbtUtils.readBlockPos(nbt.getCompound("Position1"));
            tooltip.add(Component.translatable("skyblockbuilder.item.structure_saver.position.tooltip", 1, pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (nbt.contains("Position2")) {
            BlockPos pos = NbtUtils.readBlockPos(nbt.getCompound("Position1"));
            tooltip.add(Component.translatable("skyblockbuilder.item.structure_saver.position.tooltip", 1, pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (nbt.contains("CanSave")) {
            tooltip.add(TOOLTIP_SAVE);
        } else {
            tooltip.add(TOOLTIP_INFO);
        }
    }

    @Nullable
    public static BoundingBox getArea(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains("Position1") || !nbt.contains("Position2")) {
            return null;
        }

        BlockPos pos1 = NbtUtils.readBlockPos(nbt.getCompound("Position1"));
        BlockPos pos2 = NbtUtils.readBlockPos(nbt.getCompound("Position2"));

        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static String saveSchematic(Level level, ItemStack stack, boolean ignoreAir) {
        return saveSchematic(level, stack, ignoreAir, null);
    }

    public static String saveSchematic(Level level, ItemStack stack, boolean ignoreAir, @Nullable String name) {
        StructureTemplate template = new StructureTemplate();
        BoundingBox boundingBox = getArea(stack);

        if (boundingBox == null) {
            return null;
        }

        BlockPos origin = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
        BlockPos bounds = new BlockPos(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());

        HashSet<Block> toIgnore = Sets.newHashSet(Blocks.STRUCTURE_VOID);
        if (ignoreAir) {
            toIgnore.add(Blocks.AIR);
        }
        RandomUtility.fillTemplateFromWorld(template, level, origin, bounds, true, toIgnore);

        Path path = Paths.get(RandomUtility.getFilePath(SkyPaths.MOD_EXPORTS.getFileName().toString(), name));
        try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE)) {
            CompoundTag tag = template.save(new CompoundTag());
            NbtIo.writeCompressed(tag, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return path.getFileName().toString();
    }

    public static ItemStack removeTags(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove("Position1");
        tag.remove("Position2");
        tag.remove("CanSave");
        stack.setTag(tag);
        return stack;
    }
}
