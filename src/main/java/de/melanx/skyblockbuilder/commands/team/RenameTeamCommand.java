package de.melanx.skyblockbuilder.commands.team;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockManageTeamEvent;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class RenameTeamCommand {

    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("rename")
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> renameTeam(context.getSource(), StringArgumentType.getString(context, "name"), null))
                        .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.ALL_TEAMS)
                                .executes(context -> renameTeam(context.getSource(), StringArgumentType.getString(context, "name"), StringArgumentType.getString(context, "team")))));
    }

    private static int renameTeam(CommandSourceStack source, String newName, String oldName) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        if (!RenameTeamCommand.tryRenaming(source, newName, oldName, data)) {
            return 0;
        }

        source.sendSuccess(() -> SkyComponents.SUCCESS_RENAME_TEAM.apply(newName), true);
        return 1;
    }

    private static boolean tryRenaming(CommandSourceStack source, String newName, @Nullable String oldName, SkyblockSavedData data) throws CommandSyntaxException {
        Team team;
        if (oldName == null) {
            ServerPlayer player = source.getPlayerOrException();
            team = data.getTeamFromPlayer(player);
            if (team == null) {
                source.sendFailure(SkyComponents.ERROR_USER_HAS_NO_TEAM);
                return false;
            }
        } else {
            team = data.getTeam(oldName);
            if (team == null) {
                source.sendFailure(SkyComponents.ERROR_TEAM_NOT_EXIST);
                return false;
            }
        }

        RenameResult result = processRenameRequest(source, team, newName, oldName == null);
        if (result != RenameResult.ALLOWED) {
            source.sendFailure(result.getMessage());
            return false;
        }

        ServerPlayer player = oldName == null ? source.getPlayerOrException() : null;
        SkyblockManageTeamEvent.Rename event = SkyblockHooks.onRename(player, team, newName);
        data.renameTeam(team, event.getPlayer(), event.getNewName());
        return true;
    }

    private static RenameResult processRenameRequest(CommandSourceStack source, Team team, String newName, boolean isPlayerRequest) {
        SkyblockManageTeamEvent.Rename event = SkyblockHooks.onRename(isPlayerRequest ? source.getPlayer() : null, team, newName);

        return switch (event.getResult()) {
            case DENY -> RenameResult.DENIED;
            case DEFAULT -> {
                if (!isPlayerRequest && !PermissionManager.INSTANCE.mayBypassLimitation(source)) {
                    yield RenameResult.NO_PERMISSION;
                }
                yield RenameResult.ALLOWED;
            }
            case ALLOW -> RenameResult.ALLOWED;
        };
    }

    private enum RenameResult {
        ALLOWED(null),
        DENIED(SkyComponents.DENIED_RENAME_TEAM),
        NO_PERMISSION(SkyComponents.DISABLED_RENAME_TEAM);

        private final Component message;

        RenameResult(Component message) {
            this.message = message;
        }

        public Component getMessage() {
            return this.message;
        }
    }
}
