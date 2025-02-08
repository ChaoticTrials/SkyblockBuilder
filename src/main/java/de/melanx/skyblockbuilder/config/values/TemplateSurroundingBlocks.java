package de.melanx.skyblockbuilder.config.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public record TemplateSurroundingBlocks(int margin, List<WeightedBlock> blocks) {

    public static TemplateSurroundingBlocks EMPTY = new TemplateSurroundingBlocks(0, List.of());

    public static TemplateSurroundingBlocks fromJson(JsonObject json) {
        int margin = 1;
        if (json.has("margin")) {
            margin = json.get("margin").getAsInt();
        }

        List<WeightedBlock> blocks = new ArrayList<>();
        if (json.has("blocks")) {
            json.getAsJsonArray("blocks").forEach(element -> {
                JsonObject blocksObject = element.getAsJsonObject();
                int weight = 1;
                if (blocksObject.has("weight")) {
                    weight = blocksObject.get("weight").getAsInt();
                }

                String blockId = blocksObject.get("block").getAsString();
                ResourceLocation blockLocation = ResourceLocation.tryParse(blockId);
                if (blockLocation == null) {
                    throw new IllegalArgumentException("Block id must be a valid resource location");
                }

                Block block = BuiltInRegistries.BLOCK.get(blockLocation);
                blocks.add(new WeightedBlock(block, weight));
            });
        }

        return new TemplateSurroundingBlocks(margin, blocks);
    }

    public static JsonObject toJson(TemplateSurroundingBlocks surroundingBlocks) {
        JsonObject json = new JsonObject();

        json.addProperty("margin", surroundingBlocks.margin());

        if (!surroundingBlocks.blocks().isEmpty()) {
            var blocksArray = new JsonArray();
            surroundingBlocks.blocks().forEach(weightedBlock -> {
                JsonObject blockObject = new JsonObject();
                blockObject.addProperty("block", BuiltInRegistries.BLOCK.getKey(weightedBlock.block).toString());
                blockObject.addProperty("weight", weightedBlock.weight);
                blocksArray.add(blockObject);
            });
            json.add("blocks", blocksArray);
        }

        return json;
    }

    public record WeightedBlock(Block block, int weight) implements WeightedEntry {

        @Nonnull
        @Override
        public Weight getWeight() {
            return Weight.of(this.weight);
        }
    }
}
