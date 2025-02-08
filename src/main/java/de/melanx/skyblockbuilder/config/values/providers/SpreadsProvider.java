package de.melanx.skyblockbuilder.config.values.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.values.TemplateSpreads;

public interface SpreadsProvider {

    TemplateSpreads templateSpreads();

    JsonElement toJson();

    static SpreadsProvider fromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return new Reference(json.getAsString());
        }

        if (json.isJsonObject()) {
            return new Direct(TemplateSpreads.fromJson(json.getAsJsonArray()));
        }

        throw new IllegalArgumentException("Unknown spawns: " + json);
    }

    record Reference(String name) implements SpreadsProvider {

        @Override
        public TemplateSpreads templateSpreads() {
            if (!TemplatesConfig.spreads.containsKey(this.name)) {
                throw new IllegalArgumentException("Unknown spreads: " + this.name);
            }

            return TemplatesConfig.spreads.get(this.name);
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(this.name);
        }
    }

    record Direct(TemplateSpreads templateSpreads) implements SpreadsProvider {

        @Override
        public JsonElement toJson() {
            return TemplateSpreads.toJson(this.templateSpreads);
        }
    }
}
