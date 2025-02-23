package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.CommandUtil;
import de.melanx.skyblockbuilder.util.SkyComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class AcceptCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Accepts an invitation
        return Commands.literal("accept")
                .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.INVITE_TEAMS)
                        .executes(context -> acceptTeam(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int acceptTeam(CommandSourceStack source, String teamName) throws CommandSyntaxException {
        CommandUtil.ValidationResult validationResult = CommandUtil.validateTeamExistence(source, teamName);
        if (validationResult == null) {
            return 0;
        }

        ServerPlayer player = validationResult.player();
        SkyblockSavedData data = SkyblockSavedData.get(player.level());
        Team team = validationResult.team();

        if (data.hasPlayerTeam(player)) {
            source.sendFailure(SkyComponents.ERROR_PLAYER_HAS_TEAM.apply(player.getDisplayName().getString()));
            return 0;
        }

        if (!data.hasInvites(player)) {
            source.sendFailure(SkyComponents.ERROR_NO_INVITATIONS);
            return 0;
        }

        switch (SkyblockHooks.onAccept(player, team)) {
            case DENY:
                source.sendFailure(SkyComponents.DENIED_ACCEPT_INVITATIONS);
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_INVITES)) {
                    source.sendFailure(SkyComponents.DISABLED_ACCEPT_INVITATIONS);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        if (!data.acceptInvite(team, player)) {
            source.sendFailure(SkyComponents.ERROR_ACCEPT_INVITATIONS);
            return 0;
        }

        source.sendSuccess(() -> SkyComponents.SUCCESS_JOINED_TEAM.apply(team.getName()), true);
        return 1;
    }

}
