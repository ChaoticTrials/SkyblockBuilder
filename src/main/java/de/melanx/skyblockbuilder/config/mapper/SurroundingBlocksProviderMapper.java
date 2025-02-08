package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonElement;
import de.melanx.skyblockbuilder.config.values.providers.SurroundingBlocksProvider;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

@RegisterMapper
public class SurroundingBlocksProviderMapper implements ValueMapper<SurroundingBlocksProvider, JsonElement> {

    @Override
    public Class<SurroundingBlocksProvider> type() {
        return SurroundingBlocksProvider.class;
    }

    @Override
    public Class<JsonElement> element() {
        return JsonElement.class;
    }

    @Override
    public SurroundingBlocksProvider fromJson(JsonElement json) {
        return SurroundingBlocksProvider.fromJson(json);
    }

    @Override
    public JsonElement toJson(SurroundingBlocksProvider surroundingBlocks) {
        return surroundingBlocks.toJson();
    }

    @Override
    public ConfigEditor<SurroundingBlocksProvider> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(new SurroundingBlocksProvider.Reference("default"));
    }
}
