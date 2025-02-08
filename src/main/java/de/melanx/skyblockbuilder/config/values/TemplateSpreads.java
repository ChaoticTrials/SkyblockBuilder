package de.melanx.skyblockbuilder.config.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.config.mapper.BlockPosMapper;
import de.melanx.skyblockbuilder.template.TemplateInfo;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record TemplateSpreads(List<TemplateInfo.SpreadInfo> spreads) {

    public static final TemplateSpreads EMPTY = new TemplateSpreads(List.of());

    public static TemplateSpreads fromJson(JsonArray array) {
        List<TemplateInfo.SpreadInfo> spreads = new ArrayList<>();
        array.forEach(element -> {
            JsonObject json = element.getAsJsonObject();
            if (!json.has("file") || !json.has("offset")) {
                throw new IllegalArgumentException("Spread must define file and offset");
            }

            String file = json.get("file").getAsString();
            TemplateInfo.SpreadInfo.Origin origin = json.has("origin")
                    ? TemplateInfo.SpreadInfo.Origin.valueOf(json.get("origin").getAsString().toUpperCase())
                    : TemplateInfo.SpreadInfo.Origin.CENTER;
            JsonElement offsetElement = json.get("offset");
            if (offsetElement.isJsonArray()) {
                BlockPos blockPos = BlockPosMapper.fromJsonArray(offsetElement.getAsJsonArray());
                spreads.add(new TemplateInfo.SpreadInfo(file, blockPos, blockPos, origin));
            } else {
                JsonObject offsetObject = offsetElement.getAsJsonObject();
                BlockPos minOffset = BlockPosMapper.fromJsonArray(offsetObject.getAsJsonArray("min"));
                BlockPos maxOffset = BlockPosMapper.fromJsonArray(offsetObject.getAsJsonArray("max"));
                spreads.add(new TemplateInfo.SpreadInfo(file, minOffset, maxOffset, origin));
            }
        });

        return new TemplateSpreads(spreads);
    }

    public static JsonArray toJson(TemplateSpreads spreads) {
        JsonArray array = new JsonArray();

        spreads.spreads().forEach(spread -> {
            JsonObject json = new JsonObject();
            json.addProperty("file", spread.file());
            json.addProperty("origin", spread.origin().toString().toLowerCase());

            if (spread.minOffset().equals(spread.maxOffset())) {
                json.add("offset", BlockPosMapper.toJsonArray(spread.minOffset()));
            } else {
                JsonObject offset = new JsonObject();
                offset.add("min", BlockPosMapper.toJsonArray(spread.minOffset()));
                offset.add("max", BlockPosMapper.toJsonArray(spread.maxOffset()));
                json.add("offset", offset);
            }

            array.add(json);
        });

        return array;
    }
}
