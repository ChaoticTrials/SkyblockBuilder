package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.common.InventoryConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class LeaveCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Let the player leave a team
        return Commands.literal("leave")
                .executes(context -> leaveTeam(context.getSource()));
    }

    private static int leaveTeam(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        ServerPlayer player = source.getPlayerOrException();

        if (!data.hasPlayerTeam(player)) {
            source.sendFailure(SkyComponents.ERROR_USER_HAS_NO_TEAM);
            return 0;
        }

        Team team = data.getTeamFromPlayer(player);
        switch (SkyblockHooks.onLeave(player, team)) {
            case DENY:
                source.sendFailure(SkyComponents.DENIED_LEAVE_TEAM);
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_LEAVE)) {
                    source.sendFailure(SkyComponents.DISABLED_MANAGE_TEAMS);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        if (InventoryConfig.dropItems) {
            RandomUtility.dropInventories(player);
        }
        data.removePlayerFromTeam(player);
        source.sendSuccess(() -> SkyComponents.SUCCESS_LEFT_TEAM, true);
        RandomUtility.deleteTeamIfEmpty(data, team);
        WorldUtil.teleportToIsland(player, data.getSpawn());
        return 1;
    }
}
