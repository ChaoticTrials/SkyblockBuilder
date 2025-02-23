package de.melanx.skyblockbuilder.config.values.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.values.TemplateSpreads;

public interface SpreadsProvider {

    SpreadsProvider EMPTY = new Direct(TemplateSpreads.EMPTY);

    TemplateSpreads templateSpreads();

    JsonElement toJson();

    static SpreadsProvider fromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return new Reference(json.getAsString());
        }

        if (json.isJsonArray()) {
            return new Direct(TemplateSpreads.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst());
        }

        throw new IllegalArgumentException("Unknown spreads: " + json);
    }

    record Reference(String name) implements SpreadsProvider {

        @Override
        public TemplateSpreads templateSpreads() {
            if (!TemplatesConfig.spreadReferences.containsKey(this.name)) {
                throw new IllegalArgumentException("Unknown spreads: " + this.name);
            }

            return TemplatesConfig.spreadReferences.get(this.name);
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(this.name);
        }
    }

    record Direct(TemplateSpreads templateSpreads) implements SpreadsProvider {

        @Override
        public JsonElement toJson() {
            return TemplateSpreads.CODEC.encodeStart(JsonOps.INSTANCE, this.templateSpreads).getOrThrow();
        }
    }
}
