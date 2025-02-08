package de.melanx.skyblockbuilder.data;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TemplateData extends SavedData {

    private static final String NAME = "skyblockbuilder/template";

    private final ConfiguredTemplate template;

    public TemplateData(ConfiguredTemplate template) {
        this.template = template;
        this.setDirty();
    }

    public static SavedData.Factory<TemplateData> factory() {
        ConfiguredTemplate template = TemplateLoader.getConfiguredTemplate().copy();
        return new SavedData.Factory<>(() -> new TemplateData(template), (nbt, provider) -> TemplateData.load(template, nbt));
    }

    public static TemplateData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(TemplateData.factory(), NAME);
    }

    public static TemplateData load(ConfiguredTemplate template, @Nonnull CompoundTag nbt) {
        template.read(nbt);

        return new TemplateData(template);
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        return this.template.write(tag);
    }

    @Override
    public void save(@Nonnull File file, @Nonnull HolderLookup.Provider registries) {
        if (this.isDirty()) {
            try {
                Files.createDirectories(file.toPath().getParent());
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Could not create directory: {}", file.getAbsolutePath(), e);
            }
        }

        super.save(file, registries);
    }

    public void refreshTemplate() {
        this.template.read(TemplateLoader.getConfiguredTemplate().write(new CompoundTag()));
        this.setDirty();
    }

    public ConfiguredTemplate getConfiguredTemplate() {
        return this.template;
    }
}
