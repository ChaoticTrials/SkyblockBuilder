package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.LibXConfigHandler;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockJoinRequestEvent;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class JoinCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Invites the given player
        return Commands.literal("join")
                .then(Commands.argument("team", StringArgumentType.word()).suggests(Suggestions.ALL_TEAMS)
                        .executes(context -> sendJoinRequest(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int sendJoinRequest(CommandSource source, String teamName) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);
        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeam(teamName);

        if (team == null) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.team_not_exist").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        if (data.hasPlayerTeam(player)) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.user_has_team").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        SkyblockJoinRequestEvent.SendRequest event = SkyblockHooks.onSendJoinRequest(player, team);
        switch (event.getResult()) {
            case DENY:
                source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.denied.join_request").mergeStyle(TextFormatting.RED), true);
                return 0;
            case DEFAULT:
                if (!LibXConfigHandler.Utility.selfManage && !source.hasPermissionLevel(2)) {
                    source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.disabled.join_request").mergeStyle(TextFormatting.RED), true);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        team.sendJoinRequest(player);
        source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.success.join_request", teamName).mergeStyle(TextFormatting.GOLD), true);
        return 1;
    }
}
