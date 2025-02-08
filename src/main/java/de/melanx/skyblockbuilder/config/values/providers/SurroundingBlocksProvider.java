package de.melanx.skyblockbuilder.config.values.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.values.TemplateSurroundingBlocks;

public interface SurroundingBlocksProvider {

    SurroundingBlocksProvider EMPTY = new Direct(TemplateSurroundingBlocks.EMPTY);

    TemplateSurroundingBlocks templateSurroundingBlocks();

    JsonElement toJson();

    static SurroundingBlocksProvider fromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return new Reference(json.getAsString());
        }

        if (json.isJsonObject()) {
            return new Direct(TemplateSurroundingBlocks.fromJson(json.getAsJsonObject()));
        }

        throw new IllegalArgumentException("Unknown surrounding blocks: " + json);
    }

    record Reference(String name) implements SurroundingBlocksProvider {

        @Override
        public TemplateSurroundingBlocks templateSurroundingBlocks() {
            if (!TemplatesConfig.surroundingBlocks.containsKey(this.name)) {
                throw new IllegalArgumentException("Unknown surrounding blocks: " + this.name);
            }

            return TemplatesConfig.surroundingBlocks.get(this.name);
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(this.name);
        }
    }

    record Direct(TemplateSurroundingBlocks templateSurroundingBlocks) implements SurroundingBlocksProvider {

        @Override
        public JsonElement toJson() {
            return TemplateSurroundingBlocks.toJson(this.templateSurroundingBlocks);
        }
    }
}
