package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockJoinRequestEvent;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.CommandUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class JoinCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Invites the given player
        return Commands.literal("join")
                .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.ALL_TEAMS)
                        .executes(context -> sendJoinRequest(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int sendJoinRequest(CommandSourceStack source, String teamName) throws CommandSyntaxException {
        CommandUtil.ValidationResult validationResult = CommandUtil.validateTeamExistence(source, teamName);
        if (validationResult == null) {
            return 0;
        }

        ServerPlayer player = validationResult.player();
        Team team = validationResult.team();

        if (validationResult.data().hasPlayerTeam(player)) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.user_has_team"));
            return 0;
        }

        SkyblockJoinRequestEvent.SendRequest event = SkyblockHooks.onSendJoinRequest(player, team);
        switch (event.getResult()) {
            case DENY:
                source.sendFailure(Component.translatable("skyblockbuilder.command.denied.join_request"));
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.mayExecuteOpCommand(player)) {
                    if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_INVITES)) {
                        source.sendFailure(Component.translatable("skyblockbuilder.command.disabled.join_request"));
                        return 0;
                    }

                    if (!team.allowsJoinRequests()) {
                        source.sendFailure(Component.translatable("skyblockbuilder.command.disabled.team_join_request"));
                        return 0;
                    }
                }
                break;
            case ALLOW:
                break;
        }

        team.sendJoinRequest(player);
        source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.join_request", teamName).withStyle(ChatFormatting.GOLD), true);
        return 1;
    }
}
