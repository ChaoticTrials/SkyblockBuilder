package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.InputProperties;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

import java.util.Optional;

@RegisterMapper
public class ResourceKeyMapper implements ValueMapper<ResourceKey<Level>, JsonPrimitive> {

    private static final InputProperties<ResourceKey<Level>> INPUT = new InputProperties<>() {

        @Override
        public ResourceKey<Level> defaultValue() {
            return ResourceKeyMapper.getResourceKey("overworld");
        }

        @Override
        public boolean canInputChar(char chr) {
            return Identifier.isAllowedInIdentifier(chr);
        }

        @Override
        public boolean isValid(String str) {
            return Identifier.tryParse(str) != null;
        }

        @Override
        public ResourceKey<Level> valueOf(String str) {
            return ResourceKeyMapper.getResourceKey(str);
        }

        @Override
        public String toString(ResourceKey<Level> levelResourceKey) {
            return levelResourceKey.identifier().toString();
        }
    };

    @Override
    public ResourceKey<Level> fromJson(JsonPrimitive json) {
        return ResourceKeyMapper.getResourceKey(json.getAsString());
    }

    @Override
    public JsonPrimitive toJson(ResourceKey<Level> value) {
        return new JsonPrimitive(value.identifier().toString());
    }

    @Override
    public ConfigEditor<ResourceKey<Level>> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.input(INPUT, validator);
    }


    @Override
    public Class<ResourceKey<Level>> type() {
        //noinspection unchecked
        return (Class<ResourceKey<Level>>) (Class<?>) ResourceKey.class;
    }

    @Override
    public Class<JsonPrimitive> element() {
        return JsonPrimitive.class;
    }

    @Override
    public Optional<ResourceKey<Level>> correct(JsonElement json, ConfigCorrection<ResourceKey<Level>> correction) {
        return ValueMapper.super.correct(json, correction);
    }

    private static ResourceKey<Level> getResourceKey(String str) {
        return ResourceKeyMapper.getResourceKey(Identifier.tryParse(str));
    }

    private static ResourceKey<Level> getResourceKey(Identifier location) {
        return ResourceKey.create(Registries.DIMENSION, location);
    }
}
