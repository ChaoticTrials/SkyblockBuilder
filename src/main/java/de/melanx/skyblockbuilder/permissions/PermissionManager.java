package de.melanx.skyblockbuilder.permissions;

import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;

public class PermissionManager {

    public static final PermissionManager INSTANCE = new PermissionManager();
    private static final net.minecraft.server.permissions.Permission.HasCommandLevel ALL = new net.minecraft.server.permissions.Permission.HasCommandLevel(PermissionLevel.ALL);

    private PermissionManager() {}

    public boolean hasPermission(CommandSourceStack commandSourceStack, Permission permission) {
        return INSTANCE.mayBypassLimitation(commandSourceStack)
                || commandSourceStack.permissions().hasPermission(this.mapPermissionLevel(PermissionsConfig.minimumPermissionLevelToBypass))
                || PermissionsConfig.permissions.contains(permission);
    }

    public boolean hasPermission(Player player, Permission permission) {
        return INSTANCE.mayBypassLimitation(player)
                || player.permissions().hasPermission(this.mapPermissionLevel(PermissionsConfig.minimumPermissionLevelToBypass))
                || PermissionsConfig.permissions.contains(permission);
    }

    public boolean mayBypassLimitation(CommandSourceStack commandSourceStack) {
        return commandSourceStack.permissions().hasPermission(this.mapPermissionLevel(PermissionsConfig.minimumPermissionLevelToBypassLimitations));
    }

    public boolean mayBypassLimitation(Player player) {
        return player.permissions().hasPermission(this.mapPermissionLevel(PermissionsConfig.minimumPermissionLevelToBypassLimitations));
    }

    public boolean mayExecuteOpCommand(CommandSourceStack source) {
        return source.permissions().hasPermission(this.mapPermissionLevel(PermissionsConfig.minimumPermissionLevelToExecuteCommands));
    }

    public boolean mayExecuteOpCommand(Player player) {
        return player.permissions().hasPermission(this.mapPermissionLevel(PermissionsConfig.minimumPermissionLevelToExecuteCommands));
    }

    private net.minecraft.server.permissions.Permission mapPermissionLevel(int i) {
        return switch(i) {
            case 1 -> Permissions.COMMANDS_MODERATOR;
            case 2 -> Permissions.COMMANDS_GAMEMASTER;
            case 3 -> Permissions.COMMANDS_ADMIN;
            case 4 -> Permissions.COMMANDS_OWNER;
            default -> PermissionManager.ALL;
        };
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
