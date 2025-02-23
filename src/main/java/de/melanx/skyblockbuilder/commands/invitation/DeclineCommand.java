package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.CommandUtil;
import de.melanx.skyblockbuilder.util.SkyComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class DeclineCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Declines an invitation
        return Commands.literal("decline")
                .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.INVITE_TEAMS)
                        .executes(context -> declineTeam(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int declineTeam(CommandSourceStack source, String teamName) throws CommandSyntaxException {
        CommandUtil.ValidationResult validationResult = CommandUtil.validateTeamExistence(source, teamName);
        if (validationResult == null) {
            return 0;
        }

        ServerPlayer player = validationResult.player();
        if (!validationResult.data().hasInvites(player)) {
            source.sendFailure(SkyComponents.ERROR_NO_INVITATIONS);
            return 0;
        }

        switch (SkyblockHooks.onDecline(player, validationResult.team())) {
            case DENY:
                source.sendFailure(SkyComponents.DENIED_DECLINE_INVITATIONS);
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_INVITES)) {
                    source.sendFailure(SkyComponents.DISABLED_DECLINE_INVITATIONS);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        if (!validationResult.data().declineInvite(validationResult.team(), player)) {
            source.sendFailure(SkyComponents.ERROR_DECLINE_INVITATIONS);
            return 0;
        }

        source.sendSuccess(() -> SkyComponents.SUCCESS_DECLINED_INVITATION.apply(validationResult.team().getName()), true);
        return 1;
    }
}
