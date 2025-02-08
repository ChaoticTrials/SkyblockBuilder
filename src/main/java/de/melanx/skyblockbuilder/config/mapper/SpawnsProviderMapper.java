package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonElement;
import de.melanx.skyblockbuilder.config.values.providers.SpawnsProvider;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

@RegisterMapper
public class SpawnsProviderMapper implements ValueMapper<SpawnsProvider, JsonElement> {

    @Override
    public Class<SpawnsProvider> type() {
        return SpawnsProvider.class;
    }

    @Override
    public Class<JsonElement> element() {
        return JsonElement.class;
    }

    @Override
    public SpawnsProvider fromJson(JsonElement json) {
        return SpawnsProvider.fromJson(json);
    }

    @Override
    public JsonElement toJson(SpawnsProvider provider) {
        return provider.toJson();
    }

    @Override
    public ConfigEditor<SpawnsProvider> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(new SpawnsProvider.Reference("default"));
    }
}
