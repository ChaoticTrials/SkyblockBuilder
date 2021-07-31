package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class DeclineCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Declines an invitation
        return Commands.literal("decline")
                .then(Commands.argument("team", StringArgumentType.word()).suggests(Suggestions.INVITE_TEAMS)
                        .executes(context -> declineTeam(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int declineTeam(CommandSourceStack source, String teamName) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        ServerPlayer player = source.getPlayerOrException();
        Team team = data.getTeam(teamName);

        if (team == null) {
            source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.error.team_not_exist").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        if (!data.hasInvites(player)) {
            source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.error.no_invitations").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        switch (SkyblockHooks.onDecline(player, team)) {
            case DENY:
                source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.denied.decline_invitations").withStyle(ChatFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (!ConfigHandler.Utility.selfManage && !source.hasPermission(2)) {
                    source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.disabled.decline_invitations").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        if (!data.declineInvite(team, player)) {
            source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.error.decline_invitations").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.success.declined_invitation", team.getName()).withStyle(ChatFormatting.GOLD), true);
        return 1;
    }
}
