package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockJoinRequestEvent;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class JoinCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Invites the given player
        return Commands.literal("join")
                .then(Commands.argument("team", StringArgumentType.word()).suggests(Suggestions.ALL_TEAMS)
                        .executes(context -> sendJoinRequest(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int sendJoinRequest(CommandSourceStack source, String teamName) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        ServerPlayer player = source.getPlayerOrException();
        Team team = data.getTeam(teamName);

        if (team == null) {
            source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.error.team_not_exist").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        if (data.hasPlayerTeam(player)) {
            source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.error.user_has_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        SkyblockJoinRequestEvent.SendRequest event = SkyblockHooks.onSendJoinRequest(player, team);
        switch (event.getResult()) {
            case DENY:
                source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.denied.join_request").withStyle(ChatFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (!ConfigHandler.Utility.selfManage && !source.hasPermission(2)) {
                    source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.disabled.join_request").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        team.sendJoinRequest(player);
        source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.success.join_request", teamName).withStyle(ChatFormatting.GOLD), true);
        return 1;
    }
}
