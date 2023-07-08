package de.melanx.skyblockbuilder.config.common;

import de.melanx.skyblockbuilder.SpawnProtectionEvents;
import de.melanx.skyblockbuilder.config.SpawnSettings;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.config.validate.IntRange;
import org.moddingx.libx.util.data.ResourceList;

import java.util.ArrayList;
import java.util.List;

@RegisterConfig("spawn")
public class SpawnConfig {

    @Config("The entities which you can interact with within the spawn protection")
    public static ResourceList interactionEntitiesInSpawnProtection = ResourceList.ALLOW_LIST;

    @Config("The radius of chunks where to apply spawn protection. In this area, only op players can avoid this.")
    public static int spawnProtectionRadius = 0;

    @Config({"A list of event types which will be prevented:",
            "   interact_entities = Interacting with entities, e.g. riding a pig",
            "   interact_blocks   = Interacting with blocks, e.g. activating buttons, placing, or destroying blocks",
            "   mob_griefing      = Mobs destroying the world",
            "   explosions        = TNT, creeper, or other explosions",
            "   crop_grow         = Crops increasing their growth status",
            "   mobs_spawn        = Mobs spawning",
            "   mobs_spawn_egg    = Mobs being summoned using a spawn egg",
            "   damage            = Attacking others, or getting attacked",
            "   healing           = Getting healed and saturated on spawn"})
    public static List<SpawnProtectionEvents.Type> spawnProtectionEvents = Util.make(new ArrayList<>(), list -> {
        list.add(SpawnProtectionEvents.Type.INTERACT_ENTITIES);
        list.add(SpawnProtectionEvents.Type.INTERACT_BLOCKS);
        list.add(SpawnProtectionEvents.Type.MOB_GRIEFING);
        list.add(SpawnProtectionEvents.Type.EXPLOSIONS);
        list.add(SpawnProtectionEvents.Type.CROP_GROW);
        list.add(SpawnProtectionEvents.Type.MOBS_SPAWN);
        list.add(SpawnProtectionEvents.Type.MOBS_SPAWN_EGG);
        list.add(SpawnProtectionEvents.Type.DAMAGE);
        list.add(SpawnProtectionEvents.Type.HEALING);
    });

    @Config("The radius to find a valid spawn if no given spawn is valid")
    @IntRange(min = 0)
    public static int radius = 50;

    @Config({"The dimension the islands will be generated in."})
    public static ResourceKey<Level> dimension = Level.OVERWORLD;

    public static class Height {

        @Config({"set:",
                "   Uses the bottom height of the range",
                "range_top:",
                "   Searches from the top position down to the bottom position for a valid spawn.",
                "   If no valid position was found, the top position will be used.",
                "range_bottom:",
                "   Searches from the top position down to the bottom position for a valid spawn.",
                "   If no valid position was found, the bottom position will be used."})
        public static SpawnSettings.Type spawnType = SpawnSettings.Type.SET;

        @Config({"You can set a range from minY to maxY. minY is the bottom spawn position. maxY is the top spawn dimension.",
                "If you set the spawn height type to \"set\", the bottom value will be used for a set height. " +
                        "Otherwise, the height will be calculated."})
        public static SpawnSettings.Range range = new SpawnSettings.Range(64, 319);

        @Config({"If the spawn height type is set to \"range\", this offset will be used to slightly move the spawn height in any direction.",
                "Negative values go down, positive values go up."})
        public static int offset = 0;
    }
}
