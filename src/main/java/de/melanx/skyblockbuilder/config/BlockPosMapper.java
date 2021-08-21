package de.melanx.skyblockbuilder.config;

import com.google.gson.JsonArray;
import io.github.noeppi_noeppi.libx.annotation.config.RegisterMapper;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import net.minecraft.core.BlockPos;

@RegisterMapper
public class BlockPosMapper implements ValueMapper<BlockPos, JsonArray> {

    @Override
    public BlockPos fromJson(JsonArray json) {
        if (json.size() != 3) throw new IllegalStateException("Invalid BlockPos: " + json);
        return new BlockPos(json.get(0).getAsInt(), json.get(1).getAsInt(), json.get(2).getAsInt());
    }

    @Override
    public JsonArray toJson(BlockPos value) {
        JsonArray array = new JsonArray();
        array.add(value.getX());
        array.add(value.getY());
        array.add(value.getZ());
        return array;
    }

    @Override
    public Class<BlockPos> type() {
        return BlockPos.class;
    }

    @Override
    public Class<JsonArray> element() {
        return JsonArray.class;
    }
}
