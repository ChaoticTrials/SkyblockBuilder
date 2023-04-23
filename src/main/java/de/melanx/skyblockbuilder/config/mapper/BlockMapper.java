package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.InputProperties;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

import java.util.Objects;

@RegisterMapper
public class BlockMapper implements ValueMapper<Block, JsonElement> {

    private static final InputProperties<Block> INPUT = new InputProperties<>() {
        @Override
        public Block defaultValue() {
            return Blocks.AIR;
        }

        @Override
        public Block valueOf(String str) {
            return ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(str));
        }

        @Override
        public boolean canInputChar(char chr) {
            return ResourceLocation.isAllowedInResourceLocation(chr);
        }

        @Override
        public boolean isValid(String str) {
            return ForgeRegistries.BLOCKS.containsKey(ResourceLocation.tryParse(str));
        }

        @Override
        public String toString(Block block) {
            return Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString();
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
        return ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(json.getAsString()));
    }

    @Override
    public JsonElement toJson(Block value) {
        return new JsonPrimitive(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(value), "This block doesn't exist: " + value).toString());
    }

    @Override
    public Block fromNetwork(FriendlyByteBuf buffer) {
        return ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
    }

    @Override
    public void toNetwork(Block value, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(value), "This block doesn't exist: " + value));
    }

    @Override
    public ConfigEditor<Block> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.input(INPUT);
    }
}
