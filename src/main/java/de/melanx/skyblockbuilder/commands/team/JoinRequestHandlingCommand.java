package de.melanx.skyblockbuilder.commands.team;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockJoinRequestEvent;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.CommandUtil;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class JoinRequestHandlingCommand {

    static ArgumentBuilder<CommandSourceStack, ?> registerAccept() {
        return Commands.literal("accept")
                .then(Commands.argument("player", EntityArgument.player()).suggests(Suggestions.INVITED_PLAYERS_OF_PLAYERS_TEAM)
                        .executes(context -> JoinRequestHandlingCommand.acceptRequest(context.getSource(), EntityArgument.getPlayer(context, "player"))));
    }

    static ArgumentBuilder<CommandSourceStack, ?> registerDeny() {
        return Commands.literal("deny")
                .then(Commands.argument("player", EntityArgument.player()).suggests(Suggestions.INVITED_PLAYERS_OF_PLAYERS_TEAM)
                        .executes(context -> JoinRequestHandlingCommand.denyRequest(context.getSource(), EntityArgument.getPlayer(context, "player"))));
    }

    private static int acceptRequest(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        CommandUtil.ValidationResult validationResult = JoinRequestHandlingCommand.checkJoinRequestValidity(source, player);
        if (validationResult == null) {
            return 0;
        }

        SkyblockJoinRequestEvent.AcceptRequest event = SkyblockHooks.onAcceptJoinRequest(validationResult.player(), player, validationResult.team());
        switch (event.getResult()) {
            case DENY:
                source.sendFailure(SkyComponents.DENIED_ACCEPT_JOIN_REQUEST);
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_JOIN_REQUESTS)) {
                    source.sendFailure(SkyComponents.DISABLED_ACCEPT_JOIN_REQUEST);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        validationResult.team().broadcast(SkyComponents.EVENT_ACCEPT_JOIN_REQUEST.apply(validationResult.player().getDisplayName(), player.getDisplayName()), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        validationResult.data().addPlayerToTeam(validationResult.team(), player);
        validationResult.team().removeJoinRequest(player);
        WorldUtil.teleportToIsland(player, validationResult.team());
        player.displayClientMessage(SkyComponents.SUCCESS_JOIN_REQUEST_ACCEPTED.apply(validationResult.team().getName()), false);
        return 1;
    }

    private static int denyRequest(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        CommandUtil.ValidationResult validationResult = JoinRequestHandlingCommand.checkJoinRequestValidity(source, player);
        if (validationResult == null) {
            return 0;
        }

        Team team = validationResult.team();
        SkyblockJoinRequestEvent.DenyRequest event = SkyblockHooks.onDenyJoinRequest(validationResult.player(), player, team);
        switch (event.getResult()) {
            case DENY:
                source.sendFailure(SkyComponents.DENIED_DENY_JOIN_REQUEST);
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_JOIN_REQUESTS)) {
                    source.sendFailure(SkyComponents.DISABLED_DENY_JOIN_REQUEST);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        team.broadcast(SkyComponents.EVENT_DENY_JOIN_REQUEST.apply(validationResult.player().getDisplayName(), player.getDisplayName()), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        team.removeJoinRequest(player);
        player.displayClientMessage(SkyComponents.SUCCESS_DENY_REQUEST_ACCEPTED.apply(team.getName()), false);

        return 1;
    }

    @Nullable
    private static CommandUtil.ValidationResult checkJoinRequestValidity(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        CommandUtil.ValidationResult result = CommandUtil.validatePlayerTeam(source);
        if (result == null) {
            return null;
        }

        if (result.data().hasPlayerTeam(player)) {
            source.sendFailure(SkyComponents.ERROR_PLAYER_HAS_TEAM.apply(player.getDisplayName().toString()));
            result.team().removeJoinRequest(player);
            return null;
        }

        return result;
    }
}
