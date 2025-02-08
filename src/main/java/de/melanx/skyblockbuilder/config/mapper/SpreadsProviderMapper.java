package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonElement;
import de.melanx.skyblockbuilder.config.values.providers.SpreadsProvider;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

@RegisterMapper
public class SpreadsProviderMapper implements ValueMapper<SpreadsProvider, JsonElement> {

    @Override
    public Class<SpreadsProvider> type() {
        return SpreadsProvider.class;
    }

    @Override
    public Class<JsonElement> element() {
        return JsonElement.class;
    }

    @Override
    public SpreadsProvider fromJson(JsonElement json) {
        return SpreadsProvider.fromJson(json);
    }

    @Override
    public JsonElement toJson(SpreadsProvider provider) {
        return provider.toJson();
    }

    @Override
    public ConfigEditor<SpreadsProvider> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(new SpreadsProvider.Reference("default"));
    }
}
