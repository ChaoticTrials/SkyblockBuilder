package de.melanx.skyblockbuilder.item;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.ClientUtil;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.registration.ModDataComponentTypes;
import de.melanx.skyblockbuilder.spreads.SpreadInfo;
import de.melanx.skyblockbuilder.util.*;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.moddingx.libx.config.ConfigManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ItemStructureSaver extends Item {

    private static final MutableComponent TOOLTIP_INFO = SkyComponents.ITEM_STRUCTURE_SAVER_INFO_TOOLTIP.withStyle(ChatFormatting.GOLD);
    private static final MutableComponent TOOLTIP_SAVE = SkyComponents.ITEM_STRUCTURE_SAVER_SAVE_TOOLTIP.withStyle(ChatFormatting.GOLD);
    private static final MutableComponent TOOLTIP_RESTORE = SkyComponents.ITEM_STRUCTURE_SAVER_RESTORE_TOOLTIP.withStyle(ChatFormatting.GOLD);

    public ItemStructureSaver() {
        super(new Properties().stacksTo(1));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (!context.getLevel().isClientSide && player != null && player.isShiftKeyDown()) {
            ItemStack stack = context.getItemInHand();
            CompoundTag positions = stack.get(ModDataComponentTypes.positions);

            if (positions == null) {
                positions = new CompoundTag();
            }

            if (!positions.contains("Position1")) {
                positions.put("Position1", NbtUtils.writeBlockPos(pos));
                player.displayClientMessage(SkyComponents.STRUCTURE_SAVER_POS.apply(1, pos.getX(), pos.getY(), pos.getZ()), false);
                stack.remove(ModDataComponentTypes.previousPositions);

                stack.set(ModDataComponentTypes.positions, positions);
                return InteractionResult.SUCCESS;
            }

            if (!positions.contains("Position2")) {
                positions.put("Position2", NbtUtils.writeBlockPos(pos));
                player.displayClientMessage(SkyComponents.STRUCTURE_SAVER_POS.apply(2, pos.getX(), pos.getY(), pos.getZ()), false);

                stack.set(ModDataComponentTypes.positions, positions.copy());
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean onEntitySwing(@Nonnull ItemStack stack, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand) {
        CompoundTag previousPositions = stack.get(ModDataComponentTypes.previousPositions);
        if (previousPositions != null && entity.isShiftKeyDown()) {
            ItemStructureSaver.restorePositions(stack);
        }

        return super.onEntitySwing(stack, entity, hand);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag positions = stack.get(ModDataComponentTypes.positions);

        if (positions == null) {
            return InteractionResultHolder.pass(stack);
        }

        if (!positions.contains("Position1") || !positions.contains("Position2")) {
            return InteractionResultHolder.pass(stack);
        }

        // prevent instant save
        if (!positions.contains("CanSave")) {
            positions.putBoolean("CanSave", true);
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide) {
            ClientUtil.openItemScreen(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltip, tooltipFlag);

        tooltip.add(stack.getOrDefault(ModDataComponentTypes.structureSaverType, StructureSaverSettings.Type.ISLAND).getTooltip());

        CompoundTag positions = stack.get(ModDataComponentTypes.positions);

        if (positions == null) {
            positions = new CompoundTag();
        }

        if (positions.contains("Position1")) {
            Optional<BlockPos> pos = NbtUtils.readBlockPos(positions, "Position1");
            pos.ifPresent(blockPos -> tooltip.add(SkyComponents.ITEM_STRUCTURE_SAVER_POSITION_TOOLTIP.apply(1, blockPos.getX(), blockPos.getY(), blockPos.getZ()).withStyle(ChatFormatting.DARK_GRAY)));
        }

        if (positions.contains("Position2")) {
            Optional<BlockPos> pos = NbtUtils.readBlockPos(positions, "Position2");
            pos.ifPresent(blockPos -> tooltip.add(SkyComponents.ITEM_STRUCTURE_SAVER_POSITION_TOOLTIP.apply(2, blockPos.getX(), blockPos.getY(), blockPos.getZ()).withStyle(ChatFormatting.DARK_GRAY)));
        }

        if (positions.contains("CanSave")) {
            tooltip.add(TOOLTIP_SAVE);
        } else {
            tooltip.add(TOOLTIP_INFO);
        }

        CompoundTag previousPositions = stack.get(ModDataComponentTypes.previousPositions);
        if (previousPositions != null) {
            tooltip.add(TOOLTIP_RESTORE);
        }
    }

    @Nullable
    public static BoundingBox getArea(ItemStack stack) {
        CompoundTag positions = stack.get(ModDataComponentTypes.positions);

        if (positions == null || !positions.contains("Position1") || !positions.contains("Position2")) {
            return null;
        }

        Optional<BlockPos> pos1 = NbtUtils.readBlockPos(positions, "Position1");
        Optional<BlockPos> pos2 = NbtUtils.readBlockPos(positions, "Position2");

        if (pos1.isEmpty() || pos2.isEmpty()) {
            return null;
        }

        int minX = Math.min(pos1.get().getX(), pos2.get().getX());
        int minY = Math.min(pos1.get().getY(), pos2.get().getY());
        int minZ = Math.min(pos1.get().getZ(), pos2.get().getZ());
        int maxX = Math.max(pos1.get().getX(), pos2.get().getX());
        int maxY = Math.max(pos1.get().getY(), pos2.get().getY());
        int maxZ = Math.max(pos1.get().getZ(), pos2.get().getZ());

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static String saveSchematic(Level level, ItemStack stack, StructureSaverSettings settings) {
        StructureTemplate template = new StructureTemplate();
        BoundingBox boundingBox = ItemStructureSaver.getArea(stack);

        if (boundingBox == null) {
            SkyblockBuilder.getLogger().error("No bounding box found for schematic!");
            return null;
        }

        BlockPos origin = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
        BlockPos bounds = new BlockPos(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());

        Set<Block> toIgnore = Sets.newHashSet(Blocks.STRUCTURE_VOID);
        if (settings.ignoreAir()) {
            toIgnore.add(Blocks.AIR);
        }
        Set<TemplatesConfig.Spawn> spawnPositions = RandomUtility.fillTemplateFromWorld(template, level, origin, bounds, true, toIgnore);

        if (settings.saveToConfig()) {
            return ItemStructureSaver.exportToConfig(level, stack, settings, spawnPositions, template);
        }

        if (!spawnPositions.isEmpty()) {
            Path spawns = RandomUtility.getFilePath(SkyPaths.MOD_EXPORTS, settings.name() + "_spawns", "json");
            JsonObject json = TemplateUtil.spawnsAsJson(spawnPositions);
            try {
                Files.writeString(spawns, SkyblockBuilder.PRETTY_GSON.toJson(json));
                SkyblockBuilder.getLogger().info("Saved spawns at {}", spawns.toAbsolutePath());
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Failed saving {}", spawns, e);
                return null;
            }
        }

        Path path = RandomUtility.getFilePath(SkyPaths.MOD_EXPORTS, settings.name(), settings.nbtToSnbt() ? "snbt" : "nbt");
        if (ItemStructureSaver.trySaveTemplate(settings.nbtToSnbt(), template, path)) {
            return null;
        }

        return path.getFileName().toString();
    }

    private static String exportToConfig(Level level, ItemStack stack, StructureSaverSettings settings, Set<TemplatesConfig.Spawn> spawnPositions, StructureTemplate template) {
        StructureSaverSettings.Type type = stack.getOrDefault(ModDataComponentTypes.structureSaverType, StructureSaverSettings.Type.ISLAND);
        Path configFile = SkyPaths.MOD_CONFIG.resolve("templates.json5");
        try {
            JsonObject config = SkyblockBuilder.PRETTY_GSON.fromJson(Files.readString(configFile), JsonObject.class);

            // add template
            Path templatePath = RandomUtility.getFilePath(type.getOutputPath(), settings.name(), settings.nbtToSnbt() ? "snbt" : "nbt");
            if (ItemStructureSaver.trySaveTemplate(settings.nbtToSnbt(), template, templatePath)) {
                return null;
            }

            switch (type) {
                case ISLAND -> {
                    JsonObject spawnsJson = TemplateUtil.spawnsAsJson(spawnPositions);
                    ItemStructureSaver.addSpawnsToConfig(config, spawnsJson, settings);

                    String fileName = templatePath.getFileName().toFile().getName();
                    int dot = fileName.lastIndexOf(".");
                    String templateName = fileName.substring(0, dot);

                    JsonObject islandObject = new JsonObject();
                    islandObject.addProperty("name", templateName);
                    islandObject.addProperty("file", fileName);
                    islandObject.add("spawns", spawnsJson);

                    if (!config.has("templateList")) {
                        config.add("templateList", new JsonArray());
                    }

                    JsonArray templateList = config.getAsJsonArray("templateList");
                    templateList.add(islandObject);
                    config.add("templateList", templateList);
                }
                case SPREAD -> {
                    JsonObject spreadObject = new JsonObject();
                    spreadObject.addProperty("file", templatePath.getFileName().toString());

                    JsonObject offsetObject = new JsonObject();
                    offsetObject.add("min", WorldUtil.blockPosToJsonArray(BlockPos.ZERO));
                    offsetObject.add("max", WorldUtil.blockPosToJsonArray(BlockPos.ZERO));

                    spreadObject.add("offset", offsetObject);
                    spreadObject.addProperty("origin", SpreadInfo.Origin.CENTER.toString());
                    // todo custom offsets

                    JsonArray spreadsArray = new JsonArray();
                    spreadsArray.add(spreadObject);

                    if (!config.has("spreadReferences")) {
                        config.add("spreadReferences", new JsonObject());
                    }

                    JsonObject spreadReferences = config.getAsJsonObject("spreadReferences");
                    String name = ItemStructureSaver.getAvailablePropertyName(spreadReferences, settings.name());
                    spreadReferences.add(name, spreadsArray);
                    config.add("spreadReferences", spreadReferences);
                }
            }

            // write and reload config
            Files.writeString(configFile, SkyblockBuilder.PRETTY_GSON.toJson(config));
            ConfigManager.reloadConfig(TemplatesConfig.class);
            if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
                ConfigManager.reloadConfig(TemplatesConfig.class);
//                ConfigManager.synchronize(level.getServer(), TemplatesConfig.class);
            }

            return configFile.getFileName().toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to overwrite config " + configFile.getFileName());
        }
    }

    private static void addSpawnsToConfig(JsonObject config, JsonObject json, StructureSaverSettings settings) {
        if (!config.has("spawnPointReferences")) {
            config.add("spawnPointReferences", new JsonObject());
        }

        JsonObject spawns = config.getAsJsonObject("spawnPointReferences");
        String spawnsName = ItemStructureSaver.getAvailablePropertyName(config, settings.name());
        spawns.add(spawnsName, json);
        config.add("spawnPointReferences", spawns);
    }

    private static String getAvailablePropertyName(JsonObject config, String name) {
        if (config.has(name)) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            String formattedDate = dateFormat.format(calendar.getTime());
            name = "exported_at_" + formattedDate;
        }

        return name;
    }

    private static boolean trySaveTemplate(boolean asSnbt, StructureTemplate template, Path path) {
        CompoundTag tag = template.save(new CompoundTag());
        try {
            TemplateUtil.writeTemplate(path, tag, asSnbt);
            SkyblockBuilder.getLogger().info("Saved template at {}", path.toAbsolutePath());
        } catch (IllegalStateException e) {
            SkyblockBuilder.getLogger().error("Failed saving template", e);
            return true;
        }

        return false;
    }

    public static void restorePositions(ItemStack stack) {
        CompoundTag previousPositions = stack.get(ModDataComponentTypes.previousPositions);
        if (previousPositions == null) {
            return;
        }

        CompoundTag positions = previousPositions.copy();
        positions.putBoolean("CanSave", true);
        stack.set(ModDataComponentTypes.positions, positions);
        stack.remove(ModDataComponentTypes.previousPositions);
    }

    public static ItemStack removeComponents(ItemStack stack) {
        CompoundTag positions = stack.get(ModDataComponentTypes.positions);
        if (positions == null) {
            return stack;
        }

        CompoundTag previousPositions = positions.copy();
        stack.remove(ModDataComponentTypes.positions);
        stack.set(ModDataComponentTypes.previousPositions, previousPositions);

        return stack;
    }
}
