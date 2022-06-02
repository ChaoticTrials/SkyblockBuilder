package de.melanx.skyblockbuilder.data;

import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;

public class TemplateData extends SavedData {

    private static final String NAME = "skyblock_template";

    private final ConfiguredTemplate template;

    public TemplateData(ConfiguredTemplate template) {
        this.template = template;
        this.setDirty();
    }

    public static TemplateData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        ConfiguredTemplate template = TemplateLoader.getConfiguredTemplate().copy();
        return storage.computeIfAbsent(nbt -> new TemplateData(template).load(nbt), () -> new TemplateData(template), NAME);
    }

    public TemplateData load(@Nonnull CompoundTag nbt) {
        this.template.read(nbt);

        return this;
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound) {
        return this.template.write(compound);
    }

    public void refreshTemplate() {
        this.template.read(TemplateLoader.getTemplate().save(new CompoundTag()));
        this.setDirty();
    }

    public ConfiguredTemplate getConfiguredTemplate() {
        return this.template;
    }
}
