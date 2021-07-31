package de.melanx.skyblockbuilder.data;

import de.melanx.skyblockbuilder.template.TemplateLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;

public class TemplateData extends SavedData {

    private static final String NAME = "skyblock_template";

    private final StructureTemplate template;

    public TemplateData(StructureTemplate template) {
        this.template = template;
        this.setDirty();
    }

    public static TemplateData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        StructureTemplate template = TemplateLoader.getTemplate();
        return storage.computeIfAbsent(nbt -> new TemplateData(template).load(nbt), () -> new TemplateData(template), NAME);
    }

    public TemplateData load(@Nonnull CompoundTag nbt) {
        this.template.load(nbt);

        return this;
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound) {
        return this.template.save(compound);
    }

    public void refreshTemplate() {
        this.template.load(TemplateLoader.getTemplate().save(new CompoundTag()));
        this.setDirty();
    }

    public StructureTemplate getTemplate() {
        return this.template;
    }
}
