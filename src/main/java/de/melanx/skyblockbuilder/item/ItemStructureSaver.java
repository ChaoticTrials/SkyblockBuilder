package de.melanx.skyblockbuilder.item;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.util.ClientUtility;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.TemplateUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.moddingx.libx.annotation.meta.RemoveIn;
import org.moddingx.libx.config.ConfigManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class ItemStructureSaver extends Item {

    private static final MutableComponent TOOLTIP_INFO = Component.translatable("skyblockbuilder.item.structure_saver.info.tooltip").withStyle(ChatFormatting.GOLD);
    private static final MutableComponent TOOLTIP_SAVE = Component.translatable("skyblockbuilder.item.structure_saver.save.tooltip").withStyle(ChatFormatting.GOLD);
    private static final MutableComponent TOOLTIP_RESTORE = Component.translatable("skyblockbuilder.item.structure_saver.restore.tooltip").withStyle(ChatFormatting.GOLD);

    public ItemStructureSaver() {
        super(new Properties());
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
                tag.remove("PreviousPositions");
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

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("PreviousPositions") && entity.isShiftKeyDown()) {
            ItemStructureSaver.restorePositions(stack);
        }

        return super.onEntitySwing(stack, entity);
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
            BlockPos pos = NbtUtils.readBlockPos(nbt.getCompound("Position2"));
            tooltip.add(Component.translatable("skyblockbuilder.item.structure_saver.position.tooltip", 1, pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (nbt.contains("CanSave")) {
            tooltip.add(TOOLTIP_SAVE);
        } else {
            tooltip.add(TOOLTIP_INFO);
        }

        if (nbt.contains("PreviousPositions")) {
            tooltip.add(TOOLTIP_RESTORE);
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

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.21")
    public static String saveSchematic(Level level, ItemStack stack, boolean saveToConfig, boolean ignoreAir, boolean asSnbt) {
        return saveSchematic(level, stack, saveToConfig, ignoreAir, asSnbt, false, null);
    }

    public static String saveSchematic(Level level, ItemStack stack, boolean saveToConfig, boolean ignoreAir, boolean asSnbt, boolean netherValidation) {
        return saveSchematic(level, stack, saveToConfig, ignoreAir, asSnbt, netherValidation, null);
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.21")
    public static String saveSchematic(Level level, ItemStack stack, boolean saveToConfig, boolean ignoreAir, boolean asSnbt, @Nullable String name) {
        return saveSchematic(level, stack, saveToConfig, ignoreAir, asSnbt, false, name);
    }

    public static String saveSchematic(Level level, ItemStack stack, boolean saveToConfig, boolean ignoreAir, boolean asSnbt, boolean netherValidation, @Nullable String name) {
        StructureTemplate template = new StructureTemplate();
        BoundingBox boundingBox = getArea(stack);

        if (boundingBox == null) {
            SkyblockBuilder.getLogger().error("No bounding box found for schematic!");
            return null;
        }

        if (netherValidation && level.getBlockStates(AABB.of(boundingBox)).noneMatch(state -> state.is(Blocks.NETHER_PORTAL))) {
            SkyblockBuilder.getLogger().error("No portals found for schematic!");
            return null;
        }

        BlockPos origin = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
        BlockPos bounds = new BlockPos(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());

        Set<Block> toIgnore = Sets.newHashSet(Blocks.STRUCTURE_VOID);
        if (ignoreAir) {
            toIgnore.add(Blocks.AIR);
        }
        Set<TemplatesConfig.Spawn> spawnPositions = RandomUtility.fillTemplateFromWorld(template, level, origin, bounds, true, toIgnore);

        if (saveToConfig) {
            JsonObject json = TemplateUtil.spawnsAsJson(spawnPositions);

            Path configFile = SkyPaths.MOD_CONFIG.resolve("templates.json5");
            try {
                JsonObject config = SkyblockBuilder.PRETTY_GSON.fromJson(Files.readString(configFile), JsonObject.class);
                // add spawns
                if (!config.has("spawns")) {
                    config.add("spawns", new JsonObject());
                }

                JsonObject spawns = config.getAsJsonObject("spawns");
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                String formattedDate = dateFormat.format(calendar.getTime());
                String spawnsName = "exported_at_" + formattedDate;
                spawns.add(spawnsName, json);
                config.add("spawns", spawns);

                // add template
                Path templatePath = RandomUtility.getFilePath(SkyPaths.TEMPLATES_DIR, name, asSnbt ? "snbt" : "nbt");
                CompoundTag tag = template.save(new CompoundTag());
                try {
                    TemplateUtil.writeTemplate(templatePath, tag, asSnbt);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    return null;
                }

                String fileName = templatePath.getFileName().toFile().getName();
                int dot = fileName.lastIndexOf(".");
                String templateName = fileName.substring(0, dot);

                JsonObject templateObject = new JsonObject();
                templateObject.addProperty("name", templateName);
                templateObject.addProperty("file", fileName);
                templateObject.addProperty("spawns", spawnsName);

                if (!config.has("templates")) {
                    config.add("templates", new JsonObject());
                }

                JsonArray templates = config.getAsJsonArray("templates");
                templates.add(templateObject);
                config.add("templates", templates);

                // write and reload config
                Files.writeString(configFile, SkyblockBuilder.PRETTY_GSON.toJson(config));
                ConfigManager.reloadConfig(TemplatesConfig.class);
                if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
                    ConfigManager.forceResync(null);
                }

                return configFile.getFileName().toString();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to overwrite config " + configFile.getFileName());
            }
        }

        if (!spawnPositions.isEmpty()) {
            Path spawns = RandomUtility.getFilePath(SkyPaths.MOD_EXPORTS, name + "_spawns", "json");
            JsonObject json = TemplateUtil.spawnsAsJson(spawnPositions);
            try {
                Files.writeString(spawns, SkyblockBuilder.PRETTY_GSON.toJson(json));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        Path path = RandomUtility.getFilePath(SkyPaths.MOD_EXPORTS, name, asSnbt ? "snbt" : "nbt");
        CompoundTag tag = template.save(new CompoundTag());
        try {
            TemplateUtil.writeTemplate(path, tag, asSnbt);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return null;
        }

        return path.getFileName().toString();
    }

    public static ItemStack restorePositions(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("PreviousPositions")) {
            return stack;
        }

        CompoundTag last = tag.getCompound("PreviousPositions");
        tag.put("Position1", last.getCompound("Position1"));
        tag.put("Position2", last.getCompound("Position2"));
        tag.putBoolean("CanSave", true);
        tag.remove("PreviousPositions");

        stack.setTag(tag);
        return stack;
    }

    public static ItemStack removeTags(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        if (tag.contains("Position1") && tag.contains("Position2")) {
            CompoundTag last = new CompoundTag();
            last.put("Position1", tag.getCompound("Position1"));
            last.put("Position2", tag.getCompound("Position2"));
            tag.put("PreviousPositions", last);
        }

        tag.remove("Position1");
        tag.remove("Position2");
        tag.remove("CanSave");
        stack.setTag(tag);
        return stack;
    }
}
