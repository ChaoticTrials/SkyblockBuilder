package de.melanx.skyblockbuilder.config.common;

import de.melanx.skyblockbuilder.permissions.PermissionManager;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.config.validate.IntRange;
import org.moddingx.libx.util.data.ResourceList;

import java.util.List;

@RegisterConfig("permissions")
public class PermissionsConfig {

    @Config({"Force the check if the world is skyblock",
            "This enables the commands in worlds without any skyblock dimension"})
    public static boolean forceSkyblockCheck = false;

    @Config({"Available permissions:",
            "   team_create",
            "   team_handle_invites",
            "   team_handle_join_requests",
            "   team_leave",
            "   edit_spawns",
            "   teleport_to_spawn",
            "   teleport_to_visiting_island",
            "   teleport_home",
            "   teleport_across_dimensions"})
    public static List<PermissionManager.Permission> permissions = List.of(
            PermissionManager.Permission.TEAM_CREATE,
            PermissionManager.Permission.TEAM_HANDLE_INVITES,
            PermissionManager.Permission.TEAM_HANDLE_JOIN_REQUESTS,
            PermissionManager.Permission.TEAM_LEAVE,
            PermissionManager.Permission.EDIT_SPAWNS,
            PermissionManager.Permission.TELEPORT_TO_SPAWN,
            PermissionManager.Permission.TELEPORT_TO_VISITING_ISLAND,
            PermissionManager.Permission.TELEPORT_HOME,
            PermissionManager.Permission.TELEPORT_ACROSS_DIMENSIONS
    );

    @Config("The minimum permission level to bypass the not-allowed permissions")
    @IntRange(min = 0, max = 4)
    public static int minimumPermissionLevelToBypass = 2;

    @Config("The minimum permission level to bypass limitations like cooldowns")
    @IntRange(min = 0, max = 4)
    public static int minimumPermissionLevelToBypassLimitations = 3;

    @Config("The minimum permission level to execute operator commands")
    @IntRange(min = 0, max = 4)
    public static int minimumPermissionLevelToExecuteCommands = 4;

    public static class Teleports {

        @Config("Should fall damage be removed when teleporting? [default: false]")
        public static boolean negateFallDamage = false;

        @Config("Should teleporting be prevented while falling? [default: false]")
        public static boolean disallowTeleportationDuringFalling = false;

        @Config("Dimensions in this list are not allowed for executing teleportation commands. Inverted behaviour if you set \"allow_list\" to true.")
        public static ResourceList teleportationDimensions = ResourceList.DENY_LIST;

        public static class Cooldowns {

            @Config("Cooldown in ticks for teleporting to spawn. [default: 3600 = 3min]")
            public static int spawnCooldown = 3600;

            @Config("Cooldown in ticks for visiting other islands. [default: 3600 = 3min]")
            public static int visitCooldown = 3600;

            @Config("Cooldown in ticks for teleporting back home. [default: 3600 = 3min]")
            public static int homeCooldown = 3600;
        }
    }

    public static class Spawns {

        @Config("The range from island center for possible spawns to add. [default: 50]")
        public static int range = 50;
    }
}
