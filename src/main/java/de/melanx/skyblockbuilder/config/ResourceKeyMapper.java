package de.melanx.skyblockbuilder.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.annotation.config.RegisterMapper;
import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.correct.ConfigCorrection;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.config.gui.InputProperties;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Optional;

@RegisterMapper
public class ResourceKeyMapper implements ValueMapper<ResourceKey<Level>, JsonPrimitive> {

    private static final InputProperties<ResourceKey<Level>> INPUT = new InputProperties<>() {

        @Override
        public ResourceKey<Level> defaultValue() {
            return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("overworld"));
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
            return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(str));
        }

        @Override
        public String toString(ResourceKey<Level> levelResourceKey) {
            return levelResourceKey.location().toString();
        }
    };

    @Override
    public ResourceKey<Level> fromJson(JsonPrimitive json) {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(json.getAsString()));
    }

    @Override
    public JsonPrimitive toJson(ResourceKey<Level> value) {
        return new JsonPrimitive(value.location().toString());
    }

    @Override
    public ResourceKey<Level> fromNetwork(FriendlyByteBuf buffer) {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation());
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
}
