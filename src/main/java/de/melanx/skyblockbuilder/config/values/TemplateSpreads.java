package de.melanx.skyblockbuilder.config.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.template.TemplateInfo;
import de.melanx.skyblockbuilder.util.WorldUtil;
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
                BlockPos blockPos = WorldUtil.blockPosFromJsonArray(offsetElement.getAsJsonArray());
                spreads.add(new TemplateInfo.SpreadInfo(file, blockPos, blockPos, origin));
            } else {
                JsonObject offsetObject = offsetElement.getAsJsonObject();
                BlockPos minOffset = WorldUtil.blockPosFromJsonArray(offsetObject.getAsJsonArray("min"));
                BlockPos maxOffset = WorldUtil.blockPosFromJsonArray(offsetObject.getAsJsonArray("max"));
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
                json.add("offset", WorldUtil.blockPosToJsonArray(spread.minOffset()));
            } else {
                JsonObject offset = new JsonObject();
                offset.add("min", WorldUtil.blockPosToJsonArray(spread.minOffset()));
                offset.add("max", WorldUtil.blockPosToJsonArray(spread.maxOffset()));
                json.add("offset", offset);
            }

            array.add(json);
        });

        return array;
    }
}
