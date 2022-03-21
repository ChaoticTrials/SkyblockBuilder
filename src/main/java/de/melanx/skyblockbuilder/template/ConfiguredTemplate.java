package de.melanx.skyblockbuilder.template;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.TemplateConfig;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ConfiguredTemplate {

    private final Set<BlockPos> defaultSpawns = new HashSet<>();
    private StructureTemplate template;
    private String name;
    private WorldUtil.Directions direction;

    public ConfiguredTemplate(TemplateInfo info) {
        StructureTemplate template = new StructureTemplate();
        CompoundTag nbt;
        try {
            File file = SkyPaths.TEMPLATES_DIR.resolve(info.file()).toFile();
            nbt = NbtIo.readCompressed(file);
            template.load(nbt);
        } catch (IOException e) {
            SkyblockBuilder.getLogger().error("Template with name " + info.file() + " is incorrect.", e);
        }

        this.template = template;
        this.defaultSpawns.addAll(TemplateConfig.spawns.get(info.spawns()));
        this.name = info.name();
        this.direction = info.direction();
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

    public WorldUtil.Directions getDirection() {
        return this.direction;
    }

    @Nonnull
    public CompoundTag write(CompoundTag nbt) {
        CompoundTag template = this.template.save(new CompoundTag());

        ListTag spawns = new ListTag();
        for (BlockPos pos : this.defaultSpawns) {
            CompoundTag posTag = new CompoundTag();
            posTag.putDouble("posX", pos.getX() + 0.5);
            posTag.putDouble("posY", pos.getY());
            posTag.putDouble("posZ", pos.getZ() + 0.5);

            spawns.add(posTag);
        }

        nbt.put("Template", template);
        nbt.put("Spawns", spawns);
        nbt.putString("Name", this.name);

        return nbt;
    }

    public void read(CompoundTag nbt) {
        if (nbt == null) return;
        StructureTemplate template = new StructureTemplate();
        template.load(nbt.getCompound("Template"));
        this.template = template;

        ListTag spawns = nbt.getList("Spawns", Tag.TAG_COMPOUND);
        this.defaultSpawns.clear();
        for (Tag pos : spawns) {
            CompoundTag posTag = (CompoundTag) pos;
            this.defaultSpawns.add(new BlockPos(posTag.getDouble("posX"), posTag.getDouble("posY"), posTag.getDouble("posZ")));
        }

        this.name = nbt.getString("Name");
    }

    public static ConfiguredTemplate fromTag(@Nonnull CompoundTag nbt) {
        ConfiguredTemplate info = new ConfiguredTemplate();
        info.read(nbt);
        return info;
    }
}
