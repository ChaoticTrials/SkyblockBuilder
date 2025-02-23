package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.melanx.skyblockbuilder.world.flat.FlatLayers;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

import java.util.Optional;

@RegisterMapper
public class FlatLayersMapper implements ValueMapper<FlatLayers, JsonElement> {

    @Override
    public Class<FlatLayers> type() {
        return FlatLayers.class;
    }

    @Override
    public Class<JsonElement> element() {
        return JsonElement.class;
    }

    @Override
    public FlatLayers fromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return FlatLayers.of(json.getAsString());
        }

        if (json.isJsonArray()) {
            return FlatLayers.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
        }

        throw new IllegalArgumentException("Invalid json element: " + json);
    }

    @Override
    public JsonElement toJson(FlatLayers layers) {
        Optional<JsonElement> result = FlatLayers.CODEC.encodeStart(JsonOps.INSTANCE, layers).result();
        if (result.isPresent()) {
            return result.get();
        }

        throw new IllegalArgumentException("Invalid layers: " + layers);
    }

    @Override
    public ConfigEditor<FlatLayers> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(FlatLayers.EMPTY);
    }
}
