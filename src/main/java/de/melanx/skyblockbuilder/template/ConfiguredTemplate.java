package de.melanx.skyblockbuilder.template;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.ticks.LevelTicks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ConfiguredTemplate {

    private final Set<TemplatesConfig.Spawn> defaultSpawns = new HashSet<>();
    private StructureTemplate template;
    private String name;
    private String desc;
    private BlockPos offset;
    private int surroundingMargin;
    private WeightedRandomList<TemplateSurroundingBlocks.WeightedBlock> surroundingBlocks;
    private TemplateSpreads templateSpreads;
    private boolean allowPaletteSelection;

    public ConfiguredTemplate(TemplateInfo info) {
        StructureTemplate template = new StructureTemplate();
        CompoundTag nbt;
        try {
            Path file = SkyPaths.ISLANDS_DIR.resolve(info.file());
            nbt = TemplateUtil.readTemplate(file);
            template.load(BuiltInRegistries.BLOCK.asLookup(), nbt);
        } catch (IOException | CommandSyntaxException e) {
            SkyblockBuilder.getLogger().error("Template with name {} is incorrect.", info.file(), e);
        }

        this.template = template;
        this.defaultSpawns.addAll(ConfiguredTemplate.collectSpawns(info.spawns().templateSpawns()));
        this.name = info.name();
        this.desc = info.desc();
        this.offset = info.offset();
        this.surroundingMargin = info.surroundingBlocks().templateSurroundingBlocks().margin();
        this.surroundingBlocks = WeightedRandomList.create(info.surroundingBlocks().templateSurroundingBlocks().blocks());
        this.templateSpreads = info.spreads().templateSpreads();
        this.allowPaletteSelection = info.allowPaletteSelection();
    }

    private void generateSpreads(LevelTicks<Block> blockTicks, ServerLevel serverLevel, @Nullable Team team, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags) {
        for (Either<SingleSpreadEntry, GroupWeightedSpreadEntry> either : this.templateSpreads.spreads()) {
            either.ifLeft(entry -> {
                this.placeSingleSpread(entry, blockTicks, serverLevel, team, pos, settings, random, flags);
            }).ifRight(weightedSpread -> {
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

    private ConfiguredTemplate() {}

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

    public WeightedRandomList<TemplateSurroundingBlocks.WeightedBlock> getSurroundingBlocks() {
        return this.surroundingBlocks;
    }

    public boolean allowPaletteSelection() {
        return this.allowPaletteSelection;
    }

    @Nonnull
    public CompoundTag write(CompoundTag nbt) {
        CompoundTag template = this.template.save(new CompoundTag());

        ListTag spawns = new ListTag();
        for (TemplatesConfig.Spawn spawn : this.defaultSpawns) {
            BlockPos pos = spawn.pos();
            CompoundTag posTag = WorldUtil.blockPosToTag(pos);
            posTag.putString("Direction", spawn.direction().name());

            spawns.add(posTag);
        }

        nbt.put("Template", template);
        nbt.put("Spawns", spawns);
        nbt.putString("Name", this.name);
        nbt.putString("Desc", this.desc);

        nbt.put("Offset", WorldUtil.blockPosToTag(this.offset));
        nbt.putInt("SurroundingMargin", this.surroundingMargin);

        ListTag surroundingBlocks = new ListTag();
        this.surroundingBlocks.unwrap().forEach(weightedBlock -> {
            CompoundTag blockAndWeight = new CompoundTag();
            blockAndWeight.putString("block", Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(weightedBlock.block()), "This block doesn't exist: " + weightedBlock.block()).toString());
            blockAndWeight.putInt("weight", Math.max(weightedBlock.weight(), 1));
            surroundingBlocks.add(blockAndWeight);
        });
        nbt.put("SurroundingBlocks", surroundingBlocks);

        DataResult<Tag> encode = TemplateSpreads.CODEC.encodeStart(NbtOps.INSTANCE, this.templateSpreads);
        encode.resultOrPartial(SkyblockBuilder.getLogger()::error).ifPresent(tag -> nbt.put("Spreads", tag));

        return nbt;
    }

    public void read(CompoundTag nbt) {
        if (nbt == null) return;
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("Template"));
        this.template = template;

        ListTag spawns = nbt.getList("Spawns", Tag.TAG_COMPOUND);
        this.defaultSpawns.clear();
        for (Tag tag : spawns) {
            CompoundTag posTag = (CompoundTag) tag;
            BlockPos pos = WorldUtil.blockPosFromTag(posTag);
            WorldUtil.SpawnDirection direction = WorldUtil.SpawnDirection.valueOf(posTag.getString("Direction"));
            this.defaultSpawns.add(new TemplatesConfig.Spawn(pos, direction));
        }

        this.name = nbt.getString("Name");
        this.desc = nbt.getString("Desc");
        this.offset = WorldUtil.blockPosFromTag(nbt.getCompound("Offset"));
        this.surroundingMargin = nbt.getInt("SurroundingMargin");

        ListTag surroundingBlocks = nbt.getList("SurroundingBlocks", Tag.TAG_STRING);
        List<TemplateSurroundingBlocks.WeightedBlock> blocks = new ArrayList<>();
        for (Tag tag : surroundingBlocks) {
            CompoundTag blockAndWeight = (CompoundTag) tag;
            //noinspection DataFlowIssue
            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(blockAndWeight.get("block").getAsString()));
            int weight = blockAndWeight.getInt("weight");
            blocks.add(new TemplateSurroundingBlocks.WeightedBlock(block, weight));
        }
        this.surroundingBlocks = WeightedRandomList.create(blocks);
        this.templateSpreads = TemplateSpreads.CODEC.decode(NbtOps.INSTANCE, nbt.get("Spreads")).resultOrPartial().orElseGet(() -> Pair.of(TemplateSpreads.EMPTY, new CompoundTag())).getFirst();
    }

    public ConfiguredTemplate copy() {
        CompoundTag nbt = this.write(new CompoundTag());
        return ConfiguredTemplate.fromTag(nbt);
    }

    public static ConfiguredTemplate fromTag(@Nonnull CompoundTag nbt) {
        ConfiguredTemplate info = new ConfiguredTemplate();
        info.read(nbt);
        return info;
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
                template.load(BuiltInRegistries.BLOCK.asLookup(), nbt);
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
