package de.melanx.skyblockbuilder.data;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
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

    @Override
    public void save(@Nonnull File file) {
        if (this.isDirty()) {
            try {
                Files.createDirectories(file.toPath().getParent());
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Could not create directory: {}", file.getAbsolutePath(), e);
            }
        }

        super.save(file);
    }

    public void refreshTemplate() {
        this.template.read(TemplateLoader.getConfiguredTemplate().write(new CompoundTag()));
        this.setDirty();
    }

    public ConfiguredTemplate getConfiguredTemplate() {
        return this.template;
    }
}
