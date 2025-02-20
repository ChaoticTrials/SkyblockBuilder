package de.melanx.skyblockbuilder.permissions;

import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

public class PermissionManager {

    public static final PermissionManager INSTANCE = new PermissionManager();

    private PermissionManager() {}

    public boolean hasPermission(CommandSourceStack commandSourceStack, Permission permission) {
        return commandSourceStack.hasPermission(PermissionsConfig.minimumPermissionLevelToBypass)
                || PermissionsConfig.permissions.contains(permission);
    }

    public boolean hasPermission(Player player, Permission permission) {
        return player.hasPermissions(PermissionsConfig.minimumPermissionLevelToBypass)
                || PermissionsConfig.permissions.contains(permission);
    }

    public boolean mayBypassLimitation(CommandSourceStack commandSourceStack) {
        return commandSourceStack.hasPermission(PermissionsConfig.minimumPermissionLevelToBypassLimitations);
    }

    public boolean mayBypassLimitation(Player player) {
        return player.hasPermissions(PermissionsConfig.minimumPermissionLevelToBypassLimitations);
    }

    public boolean mayExecuteOpCommand(CommandSourceStack source) {
        return source.hasPermission(PermissionsConfig.minimumPermissionLevelToExecuteCommands);
    }

    public boolean mayExecuteOpCommand(Player player) {
        return player.hasPermissions(PermissionsConfig.minimumPermissionLevelToExecuteCommands);
    }

    public enum Permission {
        TEAM_CREATE,
        TEAM_HANDLE_INVITES,
        TEAM_HANDLE_JOIN_REQUESTS,
        TEAM_LEAVE,
        EDIT_SPAWNS,
        TELEPORT_TO_SPAWN,
        TELEPORT_TO_VISITING_ISLAND,
        TELEPORT_HOME,
        TELEPORT_ACROSS_DIMENSIONS
    }
}
