package de.melanx.skyblockbuilder.template;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.TemplateConfig;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ConfiguredTemplate {

    @SuppressWarnings("deprecation")
    private static final HolderLookup<Block> BLOCK_HOLDER_LOOKUP = BuiltInRegistries.BLOCK.asLookup();
    private final Set<BlockPos> defaultSpawns = new HashSet<>();
    private StructureTemplate template;
    private String name;
    private String desc;
    private WorldUtil.Directions direction;
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
            template.load(BLOCK_HOLDER_LOOKUP, nbt);
        } catch (IOException | CommandSyntaxException e) {
            SkyblockBuilder.getLogger().error("Template with name " + info.file() + " is incorrect.", e);
        }

        this.template = template;
        this.defaultSpawns.addAll(TemplateConfig.spawns.get(info.spawns()));
        this.name = info.name();
        this.desc = info.desc();
        this.direction = info.direction();
        this.offset = info.offset();
        this.surroundingMargin = info.surroundingMargin();
        List<Block> blockPalette = TemplateConfig.surroundingBlocks.get(info.surroundingBlocks());
        if (blockPalette != null) {
            this.surroundingBlocks = List.copyOf(blockPalette);
        } else {
            this.surroundingBlocks = List.of();
        }
    }

    private ConfiguredTemplate() {
    }

    public StructureTemplate getTemplate() {
        return this.template;
    }

    public Set<BlockPos> getDefaultSpawns() {
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

    public WorldUtil.Directions getDirection() {
        return this.direction;
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
        for (BlockPos pos : this.defaultSpawns) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("posX", pos.getX());
            posTag.putInt("posY", pos.getY());
            posTag.putInt("posZ", pos.getZ());

            spawns.add(posTag);
        }

        nbt.put("Template", template);
        nbt.put("Spawns", spawns);
        nbt.putString("Name", this.name);
        nbt.putString("Desc", this.desc);
        nbt.putString("Direction", this.direction == null ? WorldUtil.Directions.SOUTH.toString() : this.direction.toString());
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
        template.load(BLOCK_HOLDER_LOOKUP, nbt.getCompound("Template"));
        this.template = template;

        ListTag spawns = nbt.getList("Spawns", Tag.TAG_COMPOUND);
        this.defaultSpawns.clear();
        for (Tag pos : spawns) {
            CompoundTag posTag = (CompoundTag) pos;
            this.defaultSpawns.add(new BlockPos(posTag.getInt("posX"), posTag.getInt("posY"), posTag.getInt("posZ")));
        }

        this.name = nbt.getString("Name");
        this.desc = nbt.getString("Desc");
        this.direction = WorldUtil.Directions.valueOf(nbt.getString("Direction"));
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
