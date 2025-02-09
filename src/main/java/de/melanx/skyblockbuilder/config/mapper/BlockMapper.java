package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.InputProperties;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

@RegisterMapper
public class BlockMapper implements ValueMapper<Block, JsonElement> {

    private static final InputProperties<Block> INPUT = new InputProperties<>() {
        @Override
        public Block defaultValue() {
            return Blocks.AIR;
        }

        @Override
        public Block valueOf(String str) {
            return BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(str));
        }

        @Override
        public boolean canInputChar(char chr) {
            return ResourceLocation.isAllowedInResourceLocation(chr);
        }

        @Override
        public boolean isValid(String str) {
            ResourceLocation id = ResourceLocation.tryParse(str);
            return id != null && BuiltInRegistries.BLOCK.containsKey(id);
        }

        @Override
        public String toString(Block block) {
            return BuiltInRegistries.BLOCK.getKey(block).toString();
        }
    };

    @Override
    public Class<Block> type() {
        return Block.class;
    }

    @Override
    public Class<JsonElement> element() {
        return JsonElement.class;
    }

    @Override
    public Block fromJson(JsonElement json) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(json.getAsString()));
    }

    @Override
    public JsonElement toJson(Block value) {
        return new JsonPrimitive(BuiltInRegistries.BLOCK.getKey(value).toString());
    }

    @Override
    public ConfigEditor<Block> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.input(INPUT);
    }
}
