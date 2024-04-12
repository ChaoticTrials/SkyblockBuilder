package de.melanx.skyblockbuilder.config.common;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.util.data.ResourceList;

@RegisterConfig("permissions")
public class PermissionsConfig {

    @Config({"Force the check if the world is skyblock",
            "This enables the commands in worlds without any skyblock dimension",
            "USE AT YOUR OWN RISK, NO SUPPORT FOR DEFAULT WORLDS WITH THIS ENABLED"})
    public static boolean forceSkyblockCheck = false;

    @Config("Should players be able to leave their team or invite others? [default: true]")
    public static boolean selfManage = true;

    @Config("Should players be able to create their own team? [default: false]")
    public static boolean createOwnTeam = false;

    public static class Teleports {

        @Config("Should fall damage be removed when teleporting? [default: false]")
        public static boolean noFallDamage = false;

        @Config("Should teleporting be prevented while falling? [default: false]")
        public static boolean preventWhileFalling = false;

        @Config("Should players be able to teleport to spawn? [default: true]")
        public static boolean spawn = true;

        @Config("Cooldown in ticks for teleporting to spawn. [default: 3600 = 3min]")
        public static int spawnCooldown = 3600;

        @Config("Should players be able to visit other island? [default: true]")
        public static boolean allowVisits = true;

        @Config("Cooldown in ticks for visiting other islands. [default: 3600 = 3min]")
        public static int visitCooldown = 3600;

        @Config("Should players be able to teleport to their home island? [default: true]")
        public static boolean home = true;

        @Config("Cooldown in ticks for teleporting back home. [default: 3600 = 3min]")
        public static int homeCooldown = 3600;

        @Config("Should players be able to teleport to another dimension? [default: true]")
        public static boolean crossDimensionTeleportation = true;

        @Config("Dimensions in this list are not allowed for executing teleportation commands. Inverted behaviour if you set \"allow_list\" to true.")
        public static ResourceList teleportationDimensions = ResourceList.DENY_LIST;
    }

    public static class Spawns {

        @Config("The range from island center for possible spawns to add. [default: 50]")
        public static int range = 50;

        @Config("Should players be able to modify their spawn positions? [default: true]")
        public static boolean modifySpawns = true;
    }
}
