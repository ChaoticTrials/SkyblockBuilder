package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockInvitationEvent;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class InviteCommand {

    public static HoverEvent COPY_TEXT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("skyblockbuilder.command.info.click_to_copy"));

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Invites the given player
        return Commands.literal("invite")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> invitePlayer(context.getSource(), EntityArgument.getPlayer(context, "player"))));
    }

    private static int invitePlayer(CommandSourceStack source, ServerPlayer invitePlayer) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        ServerPlayer player = source.getPlayerOrException();

        Team team = data.getTeamFromPlayer(player);
        if (team == null) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        Team invitedPlayersTeam = data.getTeamFromPlayer(invitePlayer);
        if (invitedPlayersTeam != null) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.player_has_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        if (data.hasInviteFrom(team, invitePlayer)) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.player_already_invited").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        SkyblockInvitationEvent.Invite event = SkyblockHooks.onInvite(invitePlayer, team, player);
        switch (event.getResult()) {
            case DENY:
                source.sendSuccess(Component.translatable("skyblockbuilder.command.denied.invite_player").withStyle(ChatFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (!ConfigHandler.Utility.selfManage && !source.hasPermission(2)) {
                    source.sendSuccess(Component.translatable("skyblockbuilder.command.disabled.send_invitations").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        data.addInvite(team, event.getInvitor(), invitePlayer);

        MutableComponent invite = Component.translatable("skyblockbuilder.command.info.invited_to_team0", player.getDisplayName().getString(), team.getName()).withStyle(ChatFormatting.GOLD);
        invite.append(Component.literal("/skyblock accept \"" + team.getName() + "\"").setStyle(Style.EMPTY
                .withHoverEvent(COPY_TEXT)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skyblock accept \"" + team.getName() + "\""))
                .applyFormat(ChatFormatting.UNDERLINE).applyFormat(ChatFormatting.GOLD)));
        invite.append(Component.translatable("skyblockbuilder.command.info.invited_to_team1").withStyle(ChatFormatting.GOLD));
        invitePlayer.displayClientMessage(invite, false);

        return 1;
    }
}
