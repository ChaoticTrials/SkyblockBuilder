package de.melanx.skyblockbuilder.config.values.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.values.TemplateSpawns;

import javax.annotation.Nonnull;

public interface SpawnsProvider {

    TemplateSpawns spawns();

    JsonElement toJson();

    static SpawnsProvider fromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return new Reference(json.getAsString());
        }

        if (json.isJsonObject()) {
            return new Direct(TemplateSpawns.fromJson(json.getAsJsonObject()));
        }

        throw new IllegalArgumentException("Unknown spawns: " + json);
    }

    record Reference(String name) implements SpawnsProvider {

        @Override
        public TemplateSpawns spawns() {
            if (!TemplatesConfig.spawns.containsKey(this.name)) {
                throw new IllegalArgumentException("Unknown spawns: " + this.name);
            }

            return TemplatesConfig.spawns.get(this.name);
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(this.name);
        }
    }

    record Direct(@Nonnull TemplateSpawns spawns) implements SpawnsProvider {

        @Override
        public JsonElement toJson() {
            return TemplateSpawns.toJson(this.spawns);
        }
    }
}
