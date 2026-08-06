package de.melanx.skyblockbuilder.data;

import com.mojang.serialization.Codec;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

public class TemplateData extends SavedData {

    private static final Identifier ID = SkyblockBuilder.getInstance().id("skyblockbuilder/template");
    public static final Codec<TemplateData> CODEC = ConfiguredTemplate.CODEC.xmap(TemplateData::new, data -> data.template);

    private ConfiguredTemplate template;

    public TemplateData(ConfiguredTemplate template) {
        this.template = template;
    }

    public static SavedDataType<TemplateData> type() {
        return new SavedDataType<>(ID, _ -> new TemplateData(TemplateLoader.getConfiguredTemplate().copy()), _ -> CODEC);
    }

    public static TemplateData get(ServerLevel level) {
        SavedDataStorage storage = level.getServer().overworld().getDataStorage();

        return storage.computeIfAbsent(TemplateData.type());
    }

    public void refreshTemplate() {
        this.template = TemplateLoader.getConfiguredTemplate().copy();
        this.setDirty();
    }

    public ConfiguredTemplate getConfiguredTemplate() {
        return this.template;
    }
}
