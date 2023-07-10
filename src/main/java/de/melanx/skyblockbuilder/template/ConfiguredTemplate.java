package de.melanx.skyblockbuilder.template;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
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

    public ConfiguredTemplate(TemplateInfo info) {
        StructureTemplate template = new StructureTemplate();
        CompoundTag nbt;
        try {
            Path file = SkyPaths.TEMPLATES_DIR.resolve(info.file());
            nbt = file.toString().endsWith(".snbt")
                    ? NbtUtils.snbtToStructure(IOUtils.toString(Files.newBufferedReader(file)))
                    : NbtIo.readCompressed(file.toFile());
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
    }

    private ConfiguredTemplate() {
    }

    private static Set<TemplatesConfig.Spawn> collectSpawns(Map<String, Set<BlockPos>> spawnMap) {
        Set<TemplatesConfig.Spawn> spawns = new HashSet<>();
        for (Map.Entry<String, Set<BlockPos>> entry : spawnMap.entrySet()) {
            WorldUtil.Directions direction = WorldUtil.Directions.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
            entry.getValue().forEach(pos -> spawns.add(new TemplatesConfig.Spawn(pos, direction)));
        }

        return spawns;
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
        return (this.name.startsWith("{") && this.name.endsWith("}")) ? Component.translatable(this.name) : Component.literal(this.name);
    }

    public Component getDescriptionComponent() {
        return (this.desc.startsWith("{") && this.desc.endsWith("}")) ? Component.translatable(this.desc) : Component.literal(this.desc);
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
}
