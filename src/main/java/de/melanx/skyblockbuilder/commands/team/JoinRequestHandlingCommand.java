package de.melanx.skyblockbuilder.commands.team;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockJoinRequestEvent;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
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
        ValidationResult validationResult = JoinRequestHandlingCommand.checkJoinRequestValidity(source, player);
        if (validationResult == null) {
            return 0;
        }

        SkyblockJoinRequestEvent.AcceptRequest event = SkyblockHooks.onAcceptJoinRequest(validationResult.player(), player, validationResult.team());
        switch (event.getResult()) {
            case DENY:
                source.sendFailure(Component.translatable("skyblockbuilder.command.denied.accept_join_request"));
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_JOIN_REQUESTS)) {
                    source.sendFailure(Component.translatable("skyblockbuilder.command.disabled.accept_join_request"));
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        validationResult.team().broadcast(Component.translatable("skyblockbuilder.event.accept_join_request", validationResult.player().getDisplayName(), player.getDisplayName()), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        validationResult.data().addPlayerToTeam(validationResult.team(), player);
        validationResult.team().removeJoinRequest(player);
        WorldUtil.teleportToIsland(player, validationResult.team());
        player.displayClientMessage(Component.translatable("skyblockbuilder.command.success.join_request_accepted", validationResult.team().getName()).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int denyRequest(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        ValidationResult validationResult = JoinRequestHandlingCommand.checkJoinRequestValidity(source, player);
        if (validationResult == null) {
            return 0;
        }

        Team team = validationResult.team();
        SkyblockJoinRequestEvent.DenyRequest event = SkyblockHooks.onDenyJoinRequest(validationResult.player(), player, team);
        switch (event.getResult()) {
            case DENY:
                source.sendFailure(Component.translatable("skyblockbuilder.command.denied.deny_join_request"));
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_JOIN_REQUESTS)) {
                    source.sendFailure(Component.translatable("skyblockbuilder.command.disabled.deny_join_request"));
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        team.broadcast(Component.translatable("skyblockbuilder.event.deny_join_request", validationResult.player().getDisplayName(), player.getDisplayName()), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        team.removeJoinRequest(player);
        player.displayClientMessage(Component.translatable("skyblockbuilder.command.success.deny_request_accepted", team.getName()).withStyle(ChatFormatting.GOLD), false);

        return 1;
    }

    @Nullable
    private static ValidationResult checkJoinRequestValidity(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        ServerPlayer commandPlayer = source.getPlayerOrException();
        Team team = data.getTeamFromPlayer(commandPlayer);
        if (team == null) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED));
            return null;
        }

        if (data.hasPlayerTeam(player)) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.player_has_team"));
            team.removeJoinRequest(player);
            return null;
        }

        return new ValidationResult(data, commandPlayer, team);
    }

    private record ValidationResult(SkyblockSavedData data, ServerPlayer player, Team team) {}
}
