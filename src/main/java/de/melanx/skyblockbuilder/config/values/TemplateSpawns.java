package de.melanx.skyblockbuilder.config.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.config.mapper.BlockPosMapper;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record TemplateSpawns(Set<BlockPos> south, Set<BlockPos> west, Set<BlockPos> north, Set<BlockPos> east) {

    public static TemplateSpawns fromJson(JsonObject json) {
        Map<String, Set<BlockPos>> spawns = new HashMap<>();
        json.entrySet().forEach(entry -> {
            entry.getValue().getAsJsonArray().forEach(array -> {
                BlockPos pos = BlockPosMapper.fromJsonArray(array.getAsJsonArray());
                spawns.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).add(pos);
            });
        });

        return TemplateSpawns.fromMap(spawns);
    }

    public static JsonObject toJson(TemplateSpawns spawns) {
        JsonObject json = new JsonObject();

        JsonArray southPositions = new JsonArray();
        for (BlockPos blockPos : spawns.south()) {
            JsonArray jsonArray = BlockPosMapper.toJsonArray(blockPos);
            southPositions.add(jsonArray);
        }

        JsonArray westPositions = new JsonArray();
        for (BlockPos blockPos : spawns.west()) {
            JsonArray jsonArray = BlockPosMapper.toJsonArray(blockPos);
            westPositions.add(jsonArray);
        }

        JsonArray northPositions = new JsonArray();
        for (BlockPos blockPos : spawns.north()) {
            JsonArray jsonArray = BlockPosMapper.toJsonArray(blockPos);
            northPositions.add(jsonArray);
        }

        JsonArray eastPositions = new JsonArray();
        for (BlockPos blockPos : spawns.east()) {
            JsonArray jsonArray = BlockPosMapper.toJsonArray(blockPos);
            eastPositions.add(jsonArray);
        }

        json.add("south", southPositions);
        json.add("west", westPositions);
        json.add("north", northPositions);
        json.add("east", eastPositions);

        return json;
    }

    public static TemplateSpawns fromMap(Map<String, Set<BlockPos>> map) {
        return new TemplateSpawns(
                map.getOrDefault("south", Set.of()),
                map.getOrDefault("west", Set.of()),
                map.getOrDefault("north", Set.of()),
                map.getOrDefault("east", Set.of())
        );
    }

    public boolean allEmpty() {
        return this.south().isEmpty() && this.west().isEmpty() && this.north().isEmpty() && this.east().isEmpty();
    }
}
