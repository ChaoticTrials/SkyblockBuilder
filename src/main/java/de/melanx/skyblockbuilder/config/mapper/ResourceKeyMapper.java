package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
            return ResourceLocation.isAllowedInResourceLocation(chr);
        }

        @Override
        public boolean isValid(String str) {
            return ResourceLocation.tryParse(str) != null;
        }

        @Override
        public ResourceKey<Level> valueOf(String str) {
            return ResourceKeyMapper.getResourceKey(str);
        }

        @Override
        public String toString(ResourceKey<Level> levelResourceKey) {
            return levelResourceKey.location().toString();
        }
    };

    @Override
    public ResourceKey<Level> fromJson(JsonPrimitive json) {
        return ResourceKeyMapper.getResourceKey(json.getAsString());
    }

    @Override
    public JsonPrimitive toJson(ResourceKey<Level> value) {
        return new JsonPrimitive(value.location().toString());
    }

    @Override
    public ResourceKey<Level> fromNetwork(FriendlyByteBuf buffer) {
        return ResourceKeyMapper.getResourceKey(buffer.readResourceLocation());
    }

    @Override
    public void toNetwork(ResourceKey<Level> value, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(value.location());
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
        return ResourceKeyMapper.getResourceKey(new ResourceLocation(str));
    }

    private static ResourceKey<Level> getResourceKey(ResourceLocation location) {
        return ResourceKey.create(Registries.DIMENSION, location);
    }
}
