package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.world.IslandPos;

import javax.annotation.Nonnull;
import java.util.Set;

public class TemplateUtil {

    public static JsonObject possibleSpawnsAsJson(@Nonnull Team team) {
        return TemplateUtil.spawnsAsJson(team.getIsland(), team.getPossibleSpawns());
    }

    public static JsonObject defaultSpawnsAsJson(@Nonnull Team team) {
        return TemplateUtil.spawnsAsJson(team.getIsland(), team.getDefaultPossibleSpawns());
    }

    public static JsonObject spawnsAsJson(IslandPos islandPos, Set<TemplatesConfig.Spawn> possibleSpawns) {
        JsonArray north = new JsonArray();
        JsonArray east = new JsonArray();
        JsonArray south = new JsonArray();
        JsonArray west = new JsonArray();

        for (TemplatesConfig.Spawn spawn : possibleSpawns) {
            JsonArray position = new JsonArray();
            position.add(spawn.pos().getX() % WorldConfig.islandDistance);
            position.add(spawn.pos().getY() - islandPos.getCenter().getY());
            position.add(spawn.pos().getZ() % WorldConfig.islandDistance);
            switch (spawn.direction()) {
                case NORTH -> north.add(position);
                case EAST -> east.add(position);
                case SOUTH -> south.add(position);
                case WEST -> west.add(position);
            }
        }

        JsonObject json = new JsonObject();
        json.add("north", north);
        json.add("east", east);
        json.add("south", south);
        json.add("west", west);

        return json;
    }
}
