package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonArray;
import com.mojang.serialization.JsonOps;
import de.melanx.skyblockbuilder.config.values.TemplateSpreads;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

@RegisterMapper
public class TemplateSpreadsMapper implements ValueMapper<TemplateSpreads, JsonArray> {

    @Override
    public Class<TemplateSpreads> type() {
        return TemplateSpreads.class;
    }

    @Override
    public Class<JsonArray> element() {
        return JsonArray.class;
    }

    @Override
    public TemplateSpreads fromJson(JsonArray json) {
        return TemplateSpreads.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
    }

    @Override
    public JsonArray toJson(TemplateSpreads value) {
        return TemplateSpreads.CODEC.encodeStart(JsonOps.INSTANCE, value).getOrThrow().getAsJsonArray();
    }

    @Override
    public ConfigEditor<TemplateSpreads> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(TemplateSpreads.EMPTY);
    }
}
