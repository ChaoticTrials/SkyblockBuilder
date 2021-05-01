package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.LibXConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class DeclineCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Declines an invitation
        return Commands.literal("decline")
                .then(Commands.argument("team", StringArgumentType.word()).suggests(AcceptCommand.SUGGEST_TEAMS)
                        .executes(context -> declineTeam(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int declineTeam(CommandSource source, String teamName) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);
        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeam(teamName);

        if (team == null) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.team_not_exist").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        if (!data.hasInvites(player)) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.no_invitations").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        switch (SkyblockHooks.onDecline(player, team)) {
            case DENY:
                source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.denied.decline_invitations").mergeStyle(TextFormatting.RED), true);
                return 0;
            case DEFAULT:
                if (!LibXConfigHandler.Utility.selfManage && !source.hasPermissionLevel(2)) {
                    source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.disabled.decline_invitations").mergeStyle(TextFormatting.RED), true);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        if (!data.declineInvite(team, player)) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.decline_invitations").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.success.declined_invitation", team.getName()).mergeStyle(TextFormatting.GOLD), true);
        return 1;
    }
}
