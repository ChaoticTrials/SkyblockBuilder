package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.CommandUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
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
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.no_invitations"));
            return 0;
        }

        switch (SkyblockHooks.onDecline(player, validationResult.team())) {
            case DENY:
                source.sendFailure(Component.translatable("skyblockbuilder.command.denied.decline_invitations"));
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_INVITES)) {
                    source.sendFailure(Component.translatable("skyblockbuilder.command.disabled.decline_invitations"));
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        if (!validationResult.data().declineInvite(validationResult.team(), player)) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.decline_invitations"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.declined_invitation", validationResult.team().getName()).withStyle(ChatFormatting.GOLD), true);
        return 1;
    }
}
