package de.melanx.skyblockbuilder.template;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.config.values.TemplateSpawns;
import de.melanx.skyblockbuilder.config.values.TemplateSpreads;
import de.melanx.skyblockbuilder.config.values.TemplateSurroundingBlocks;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.registration.ModBlockTags;
import de.melanx.skyblockbuilder.spreads.GroupWeightedSpreadEntry;
import de.melanx.skyblockbuilder.spreads.SingleSpreadEntry;
import de.melanx.skyblockbuilder.spreads.SpreadInfo;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.TemplateUtil;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.ticks.LevelTicks;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfiguredTemplate {

    // no vanilla codec present
    private static final Codec<StructureTemplate> TEMPLATE_CODEC = CompoundTag.CODEC.xmap(
            nbt -> {
                StructureTemplate template = new StructureTemplate();
                template.load(BuiltInRegistries.BLOCK, nbt);
                return template;
            },
            template -> template.save(new CompoundTag())
    );

    public static final Codec<ConfiguredTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TEMPLATE_CODEC.fieldOf("Template").forGetter(template -> template.template),
            TemplatesConfig.Spawn.CODEC.listOf().optionalFieldOf("Spawns", List.of()).forGetter(template -> List.copyOf(template.defaultSpawns)),
            Codec.STRING.optionalFieldOf("Name", "").forGetter(template -> template.name),
            Codec.STRING.optionalFieldOf("Desc", "").forGetter(template -> template.desc),
            BlockPos.CODEC.optionalFieldOf("Offset", BlockPos.ZERO).forGetter(template -> template.offset),
            Codec.INT.optionalFieldOf("SurroundingMargin", 0).forGetter(template -> template.surroundingMargin),
            WeightedList.codec(BuiltInRegistries.BLOCK.byNameCodec()).optionalFieldOf("SurroundingBlocks", WeightedList.of()).forGetter(template -> template.surroundingBlocks),
            TemplateSpreads.CODEC.optionalFieldOf("Spreads", TemplateSpreads.EMPTY).forGetter(template -> template.templateSpreads),
            Codec.BOOL.optionalFieldOf("AllowPaletteSelection", false).forGetter(template -> template.allowPaletteSelection)
    ).apply(instance, ConfiguredTemplate::new));

    private final Set<TemplatesConfig.Spawn> defaultSpawns = new HashSet<>();
    private final StructureTemplate template;
    private final String name;
    private final String desc;
    private final BlockPos offset;
    private final int surroundingMargin;
    private final WeightedList<Block> surroundingBlocks;
    private final TemplateSpreads templateSpreads;
    private final boolean allowPaletteSelection;

    private ConfiguredTemplate(StructureTemplate template, List<TemplatesConfig.Spawn> defaultSpawns, String name, String desc,
            BlockPos offset, int surroundingMargin, WeightedList<Block> surroundingBlocks,
            TemplateSpreads templateSpreads, boolean allowPaletteSelection) {
        this.template = template;
        this.defaultSpawns.addAll(defaultSpawns);
        this.name = name;
        this.desc = desc;
        this.offset = offset;
        this.surroundingMargin = surroundingMargin;
        this.surroundingBlocks = surroundingBlocks;
        this.templateSpreads = templateSpreads;
        this.allowPaletteSelection = allowPaletteSelection;
    }

    public ConfiguredTemplate(TemplateInfo info) {
        StructureTemplate template = new StructureTemplate();
        CompoundTag nbt;
        try {
            Path file = SkyPaths.ISLANDS_DIR.resolve(info.file());
            nbt = TemplateUtil.readTemplate(file);
            template.load(BuiltInRegistries.BLOCK, nbt);
        } catch (IOException | CommandSyntaxException e) {
            SkyblockBuilder.getLogger().error("Template with name {} is incorrect.", info.file(), e);
        }

        this.template = template;
        this.defaultSpawns.addAll(ConfiguredTemplate.collectSpawns(info.spawns().templateSpawns()));
        this.name = info.name();
        this.desc = info.desc();
        this.offset = info.offset();
        this.surroundingMargin = info.surroundingBlocks().templateSurroundingBlocks().margin();
        this.surroundingBlocks = WeightedList.of(info.surroundingBlocks().templateSurroundingBlocks().blocks().stream()
                .map(TemplateSurroundingBlocks.WeightedBlock::weighted)
                .toList());
        this.templateSpreads = info.spreads().templateSpreads();
        this.allowPaletteSelection = info.allowPaletteSelection();
    }

    private void generateSpreads(LevelTicks<Block> blockTicks, ServerLevel serverLevel, @Nullable Team team, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags) {
        for (Either<SingleSpreadEntry, GroupWeightedSpreadEntry> either : this.templateSpreads.spreads()) {
            either.ifLeft(entry -> this.placeSingleSpread(entry, blockTicks, serverLevel, team, pos, settings, random, flags))
                    .ifRight(weightedSpread -> {
                        for (SingleSpreadEntry entry : weightedSpread.chooseEntries(random)) {
                            this.placeSingleSpread(entry, blockTicks, serverLevel, team, pos, settings, random, flags);
                        }
                    });
        }
    }

    private void placeSingleSpread(SingleSpreadEntry entry, LevelTicks<Block> blockTicks, ServerLevel serverLevel, Team team, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags) {
        SpreadConfig spreadConfig = new SpreadConfig(entry.file(), entry.minOffset(), entry.maxOffset(), entry.origin());
        BlockPos offset = spreadConfig.getRandomOffset(random);
        if (spreadConfig.getOrigin() != SpreadInfo.Origin.ZERO) {
            offset = offset.offset(SpreadInfo.Origin.originOffset(spreadConfig.getOrigin(), this.template));
        }

        BlockPos offsetPos = pos.offset(offset);
        spreadConfig.getTemplate().placeInWorld(serverLevel, offsetPos, offsetPos, settings, random, flags);
        ConfiguredTemplate.clearBlockTicks(serverLevel, blockTicks, offsetPos, spreadConfig.getTemplate());
        if (team != null) {
            team.addSpread(spreadConfig.getFileNameWithoutExtension(), offsetPos, new BlockPos(spreadConfig.template.getSize()));
        }
    }

    public static void placeNetherSpreads(TemplateSpreads spreads, ServerLevel level, @Nullable Team team, BlockPos portalPos, RandomSource random, int flags) {
        if (team != null && team.isNetherSpreadsPlaced()) {
            return;
        }

        LevelTicks<Block> blockTicks = level.getBlockTicks();

        for (Either<SingleSpreadEntry, GroupWeightedSpreadEntry> either : spreads.spreads()) {
            either.ifLeft(single -> ConfiguredTemplate.placeNetherSingleSpread(single, blockTicks, level, portalPos, random, flags))
                    .ifRight(group -> {
                        for (SingleSpreadEntry entry : group.chooseEntries(random)) {
                            ConfiguredTemplate.placeNetherSingleSpread(entry, blockTicks, level, portalPos, random, flags);
                        }
                    });
        }

        if (team != null) {
            team.markNetherSpreadsPlaced();
        }
    }

    private static void placeNetherSingleSpread(SingleSpreadEntry entry, LevelTicks<Block> blockTicks, ServerLevel level, BlockPos portalPos, RandomSource random, int flags) {
        SpreadConfig spreadConfig = new SpreadConfig(entry);
        BlockPos offset = spreadConfig.getRandomOffset(random);
        if (spreadConfig.getOrigin() != SpreadInfo.Origin.ZERO) {
            offset = offset.offset(SpreadInfo.Origin.originOffset(spreadConfig.getOrigin(), spreadConfig.getTemplate()));
        }

        BlockPos offsetPos = portalPos.offset(offset);
        spreadConfig.getTemplate().placeInWorld(level, offsetPos, offsetPos, TemplateUtil.STRUCTURE_PLACE_SETTINGS, random, flags);
        ConfiguredTemplate.clearBlockTicks(level, blockTicks, offsetPos, spreadConfig.getTemplate());
    }

    private static Set<TemplatesConfig.Spawn> collectSpawns(TemplateSpawns spawns) {
        Set<TemplatesConfig.Spawn> combinedSpawns = new HashSet<>();
        spawns.south().forEach(pos -> combinedSpawns.add(new TemplatesConfig.Spawn(pos, WorldUtil.SpawnDirection.SOUTH)));
        spawns.west().forEach(pos -> combinedSpawns.add(new TemplatesConfig.Spawn(pos, WorldUtil.SpawnDirection.WEST)));
        spawns.north().forEach(pos -> combinedSpawns.add(new TemplatesConfig.Spawn(pos, WorldUtil.SpawnDirection.NORTH)));
        spawns.east().forEach(pos -> combinedSpawns.add(new TemplatesConfig.Spawn(pos, WorldUtil.SpawnDirection.EAST)));

        return combinedSpawns;
    }

    public void placeInWorld(ServerLevel serverLevel, Team team, StructurePlaceSettings settings, RandomSource random, int flags) {
        this.placeInWorld(serverLevel, team, team.getIsland().getCenter(), settings, random, flags);
    }

    public void placeInWorld(ServerLevel serverLevel, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags) {
        this.placeInWorld(serverLevel, null, pos, settings, random, flags);
    }

    public void placeInWorld(ServerLevel serverLevel, @Nullable Team team, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags) {
        LevelTicks<Block> blockTicks = serverLevel.getBlockTicks();
        this.generateSpreads(blockTicks, serverLevel, team, pos, settings, random, flags);

        this.template.placeInWorld(serverLevel, pos, pos, settings, random, flags);
        ConfiguredTemplate.clearBlockTicks(serverLevel, blockTicks, pos, this.template);
    }

    private static void clearBlockTicks(ServerLevel level, LevelTicks<Block> blockTicks, BlockPos startPos, StructureTemplate template) {
        if (!WorldConfig.preventScheduledTicks) {
            return;
        }

        BoundingBox box = BoundingBox.fromCorners(startPos, startPos.offset(template.getSize()));
        BlockPos.betweenClosedStream(box).forEach(pos -> {
            if (level.getBlockState(pos).is(ModBlockTags.PREVENT_SCHEDULED_TICK)) {
                BoundingBox oneBlockBox = BoundingBox.fromCorners(pos, pos);
                blockTicks.clearArea(oneBlockBox);
            }
        });
    }

    public StructureTemplate getTemplate() {
        return this.template;
    }

    public Set<TemplatesConfig.Spawn> getDefaultSpawns() {
        return this.defaultSpawns;
    }

    public String getName() {
        return this.name;
    }

    public Component getNameComponent() {
        return (this.name.startsWith("{") && this.name.endsWith("}")) ? Component.translatable(this.name.substring(1, this.name.length() - 1)) : Component.literal(this.name);
    }

    public Component getDescriptionComponent() {
        return (this.desc.startsWith("{") && this.desc.endsWith("}")) ? Component.translatable(this.desc.substring(1, this.desc.length() - 1)) : Component.literal(this.desc);
    }

    public BlockPos getOffset() {
        return this.offset;
    }

    public int getSurroundingMargin() {
        return this.surroundingMargin;
    }

    public WeightedList<Block> getSurroundingBlocks() {
        return this.surroundingBlocks;
    }

    public boolean allowPaletteSelection() {
        return this.allowPaletteSelection;
    }

    public boolean canSelectPalette() {
        return this.allowPaletteSelection && this.template.palettes.size() > 1;
    }

    /**
     * Deep copy via a codec round-trip -- {@link StructureTemplate} is mutable and
     * {@link #onlyWithPalette(int)} rewrites its palettes, so callers must not share one.
     */
    public ConfiguredTemplate copy() {
        Tag tag = CODEC.encodeStart(NbtOps.INSTANCE, this)
                .getOrThrow(error -> new IllegalStateException("Failed to copy configured template: " + error));

        return CODEC.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(error -> new IllegalStateException("Failed to copy configured template: " + error));
    }

    public ConfiguredTemplate onlyWithPalette(int paletteIndex) {
        ConfiguredTemplate template = this.copy();
        template.getTemplate().palettes = List.of(template.getTemplate().palettes.get(paletteIndex));
        return template;
    }

    public static class SpreadConfig {

        private final String fileName;
        private final BlockPos minOffset;
        private final BlockPos maxOffset;
        private final StructureTemplate template;
        private final SpreadInfo.Origin origin;

        public SpreadConfig(SpreadInfo info) {
            this(info.file(), info.minOffset(), info.maxOffset(), info.origin());
        }

        public SpreadConfig(String fileName, BlockPos minOffset, BlockPos maxOffset, SpreadInfo.Origin origin) {
            StructureTemplate template = new StructureTemplate();
            CompoundTag nbt;
            try {
                Path file = SkyPaths.SPREADS_DIR.resolve(fileName);
                nbt = TemplateUtil.readTemplate(file);
                template.load(BuiltInRegistries.BLOCK, nbt);
            } catch (IOException | CommandSyntaxException e) {
                SkyblockBuilder.getLogger().error("Template with file name {} is incorrect.", fileName, e);
            }

            this.fileName = fileName;
            this.minOffset = minOffset;
            this.maxOffset = maxOffset;
            this.template = template;
            this.origin = origin;
        }

        public String getFileName() {
            return this.fileName;
        }

        public String getFileNameWithoutExtension() {
            return this.fileName.substring(0, this.fileName.lastIndexOf("."));
        }

        public BlockPos getMinOffset() {
            return this.minOffset;
        }

        public BlockPos getMaxOffset() {
            return this.maxOffset;
        }

        public BlockPos getRandomOffset() {
            return this.getRandomOffset(RandomSource.create());
        }

        public BlockPos getRandomOffset(long seed) {
            return this.getRandomOffset(RandomSource.create(seed));
        }

        public BlockPos getRandomOffset(RandomSource random) {
            BlockPos offset = new BlockPos(
                    getRandomBetween(random, this.minOffset.getX(), this.maxOffset.getX()),
                    getRandomBetween(random, this.minOffset.getY(), this.maxOffset.getY()),
                    getRandomBetween(random, this.minOffset.getZ(), this.maxOffset.getZ())
            );

            if (this.getOrigin() != SpreadInfo.Origin.ZERO) {
                offset = offset.subtract(SpreadInfo.Origin.originOffset(this.origin, this.template));
            }

            return offset;
        }

        public SpreadInfo.Origin getOrigin() {
            return this.origin;
        }

        public StructureTemplate getTemplate() {
            return this.template;
        }

        private static int getRandomBetween(RandomSource random, int i, int j) {
            if (i == j) {
                return i;
            }

            int min = Math.min(i, j);
            int max = Math.max(i, j);

            return random.nextIntBetweenInclusive(min, max);
        }
    }
}
