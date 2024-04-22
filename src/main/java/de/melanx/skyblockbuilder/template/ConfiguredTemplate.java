package de.melanx.skyblockbuilder.template;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.TemplateUtil;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ConfiguredTemplate {

    private final Set<TemplatesConfig.Spawn> defaultSpawns = new HashSet<>();
    private StructureTemplate template;
    private String name;
    private String desc;
    private TemplateInfo.Offset offset;
    private int surroundingMargin;
    private List<Block> surroundingBlocks;
    private List<SpreadConfig> spreads;

    public ConfiguredTemplate(TemplateInfo info) {
        StructureTemplate template = new StructureTemplate();
        CompoundTag nbt;
        try {
            Path file = SkyPaths.TEMPLATES_DIR.resolve(info.file());
            nbt = TemplateUtil.readTemplate(file);
            //noinspection deprecation
            template.load(BuiltInRegistries.BLOCK.asLookup(), nbt);
        } catch (IOException | CommandSyntaxException e) {
            SkyblockBuilder.getLogger().error("Template with name " + info.file() + " is incorrect.", e);
        }

        this.template = template;
        this.defaultSpawns.addAll(ConfiguredTemplate.collectSpawns(TemplatesConfig.spawns.get(info.spawns())));
        this.name = info.name();
        this.desc = info.desc();
        this.offset = info.offset();
        this.surroundingMargin = info.surroundingMargin();
        List<Block> blockPalette = TemplatesConfig.surroundingBlocks.get(info.surroundingBlocks());
        if (blockPalette != null) {
            this.surroundingBlocks = List.copyOf(blockPalette);
        } else {
            this.surroundingBlocks = List.of();
        }
        List<TemplateInfo.SpreadInfo> spreadInfos = TemplatesConfig.spreads.get(info.spreads());
        List<SpreadConfig> spreadConfigs = new ArrayList<>();
        if (spreadInfos != null) {
            for (TemplateInfo.SpreadInfo spreadInfo : spreadInfos) {
                spreadConfigs.add(new SpreadConfig(spreadInfo));
            }
        }
        this.spreads = List.copyOf(spreadConfigs);
    }

    private ConfiguredTemplate() {}

    private static Set<TemplatesConfig.Spawn> collectSpawns(Map<String, Set<BlockPos>> spawnMap) {
        Set<TemplatesConfig.Spawn> spawns = new HashSet<>();
        for (Map.Entry<String, Set<BlockPos>> entry : spawnMap.entrySet()) {
            WorldUtil.Directions direction = WorldUtil.Directions.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
            entry.getValue().forEach(pos -> spawns.add(new TemplatesConfig.Spawn(pos, direction)));
        }

        return spawns;
    }

    public boolean placeInWorld(ServerLevelAccessor serverLevel, BlockPos pos, BlockPos otherPos, StructurePlaceSettings settings, RandomSource random, int flags) {
        for (SpreadConfig spread : this.spreads) {
            BlockPos offset = spread.getRandomOffset(random);
            if (spread.getOrigin() != TemplateInfo.SpreadInfo.Origin.ZERO) {
                offset = offset.offset(TemplateInfo.SpreadInfo.Origin.originOffset(spread.getOrigin(), this.template));
            }
            spread.getTemplate().placeInWorld(serverLevel, pos.offset(offset), otherPos.offset(offset), settings, random, flags);
        }
        return this.template.placeInWorld(serverLevel, pos, otherPos, settings, random, flags);
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

    public TemplateInfo.Offset getOffset() {
        return this.offset;
    }

    public int getSurroundingMargin() {
        return this.surroundingMargin;
    }

    public List<Block> getSurroundingBlocks() {
        return this.surroundingBlocks;
    }

    @Nonnull
    public CompoundTag write(CompoundTag nbt) {
        CompoundTag template = this.template.save(new CompoundTag());

        ListTag spawns = new ListTag();
        for (TemplatesConfig.Spawn spawn : this.defaultSpawns) {
            BlockPos pos = spawn.pos();
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("posX", pos.getX());
            posTag.putInt("posY", pos.getY());
            posTag.putInt("posZ", pos.getZ());
            posTag.putString("Direction", spawn.direction().name());

            spawns.add(posTag);
        }

        nbt.put("Template", template);
        nbt.put("Spawns", spawns);
        nbt.putString("Name", this.name);
        nbt.putString("Desc", this.desc);
        nbt.putInt("OffsetX", this.offset.x());
        nbt.putInt("OffsetY", this.offset.y());
        nbt.putInt("OffsetZ", this.offset.z());
        nbt.putInt("SurroundingMargin", this.surroundingMargin);

        ListTag surroundingBlocks = new ListTag();
        this.surroundingBlocks.forEach(block -> {
            StringTag tag = StringTag.valueOf(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block), "This block doesn't exist: " + block).toString());
            surroundingBlocks.add(tag);
        });
        nbt.put("SurroundingBlocks", surroundingBlocks);

        ListTag spreads = new ListTag();
        this.spreads.forEach(spread -> {
            CompoundTag minPos = new CompoundTag();
            minPos.putInt("posX", spread.minOffset.getX());
            minPos.putInt("posY", spread.minOffset.getY());
            minPos.putInt("posZ", spread.minOffset.getZ());

            CompoundTag maxPos = new CompoundTag();
            maxPos.putInt("posX", spread.maxOffset.getX());
            maxPos.putInt("posY", spread.maxOffset.getY());
            maxPos.putInt("posZ", spread.maxOffset.getZ());

            CompoundTag tag = new CompoundTag();
            tag.putString("File", spread.fileName);
            tag.putString("Origin", spread.origin.name());
            tag.put("minOffset", minPos);
            tag.put("maxOffset", maxPos);

            spreads.add(tag);
        });
        nbt.put("Spreads", spreads);

        return nbt;
    }

    public void read(CompoundTag nbt) {
        if (nbt == null) return;
        StructureTemplate template = new StructureTemplate();
        //noinspection deprecation
        template.load(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("Template"));
        this.template = template;

        ListTag spawns = nbt.getList("Spawns", Tag.TAG_COMPOUND);
        this.defaultSpawns.clear();
        for (Tag tag : spawns) {
            CompoundTag posTag = (CompoundTag) tag;
            BlockPos pos = new BlockPos(posTag.getInt("posX"), posTag.getInt("posY"), posTag.getInt("posZ"));
            WorldUtil.Directions direction = WorldUtil.Directions.valueOf(posTag.getString("Direction"));
            this.defaultSpawns.add(new TemplatesConfig.Spawn(pos, direction));
        }

        this.name = nbt.getString("Name");
        this.desc = nbt.getString("Desc");
        this.offset = new TemplateInfo.Offset(nbt.getInt("OffsetX"), nbt.getInt("OffsetY"), nbt.getInt("OffsetZ"));
        this.surroundingMargin = nbt.getInt("SurroundingMargin");

        ListTag surroundingBlocks = nbt.getList("SurroundingBlocks", Tag.TAG_STRING);
        Set<Block> blocks = new HashSet<>();
        for (Tag block : surroundingBlocks) {
            Block value = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(block.getAsString()));
            blocks.add(value);
        }
        this.surroundingBlocks = List.copyOf(blocks);

        ListTag spreads = nbt.getList("Spreads", Tag.TAG_COMPOUND);
        List<SpreadConfig> spreadConfigs = new ArrayList<>();
        for (Tag spread : spreads) {
            String file = ((CompoundTag) spread).getString("File");
            TemplateInfo.SpreadInfo.Origin origin = TemplateInfo.SpreadInfo.Origin.valueOf(((CompoundTag) spread).getString("Origin"));

            CompoundTag minPos = ((CompoundTag) spread).getCompound("minOffset");
            BlockPos minOffset = new BlockPos(minPos.getInt("posX"), minPos.getInt("posY"), minPos.getInt("posZ"));

            CompoundTag maxPos = ((CompoundTag) spread).getCompound("maxOffset");
            BlockPos maxOffset = new BlockPos(maxPos.getInt("posX"), maxPos.getInt("posY"), maxPos.getInt("posZ"));

            spreadConfigs.add(new SpreadConfig(file, minOffset, maxOffset, origin));
        }
        this.spreads = List.copyOf(spreadConfigs);
    }

    public ConfiguredTemplate copy() {
        CompoundTag nbt = this.write(new CompoundTag());
        ConfiguredTemplate template = new ConfiguredTemplate();
        template.read(nbt);

        return template;
    }

    public static ConfiguredTemplate fromTag(@Nonnull CompoundTag nbt) {
        ConfiguredTemplate info = new ConfiguredTemplate();
        info.read(nbt);
        return info;
    }

    public static class SpreadConfig {

        private final String fileName;
        private final BlockPos minOffset;
        private final BlockPos maxOffset;
        private final StructureTemplate template;
        private final TemplateInfo.SpreadInfo.Origin origin;

        public SpreadConfig(TemplateInfo.SpreadInfo info) {
            this(info.file(), info.minOffset(), info.maxOffset(), info.origin());
        }

        public SpreadConfig(String fileName, BlockPos minOffset, BlockPos maxOffset, TemplateInfo.SpreadInfo.Origin origin) {
            StructureTemplate template = new StructureTemplate();
            CompoundTag nbt;
            try {
                Path file = SkyPaths.SPREADS_DIR.resolve(fileName);
                nbt = TemplateUtil.readTemplate(file);
                //noinspection deprecation
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

            if (this.getOrigin() != TemplateInfo.SpreadInfo.Origin.ZERO) {
                offset = offset.subtract(TemplateInfo.SpreadInfo.Origin.originOffset(this.origin, this.template));
            }

            return offset;
        }

        public TemplateInfo.SpreadInfo.Origin getOrigin() {
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
