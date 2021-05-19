package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockInvitationEvent;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.server.ServerWorld;

public class InviteCommand {

    public static HoverEvent COPY_TEXT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("skyblockbuilder.command.info.click_to_copy"));

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Invites the given player
        return Commands.literal("invite")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> invitePlayer(context.getSource(), EntityArgument.getPlayer(context, "player"))));
    }

    private static int invitePlayer(CommandSource source, ServerPlayerEntity invitePlayer) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);
        ServerPlayerEntity player = source.asPlayer();

        Team team = data.getTeamFromPlayer(player);
        if (team == null) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.user_has_no_team").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        Team invitedPlayersTeam = data.getTeamFromPlayer(invitePlayer);
        if (invitedPlayersTeam != null) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.player_has_team").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        if (data.hasInvites(invitePlayer)) {
            if (data.hasInviteFrom(team, invitePlayer)) {
                source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.player_already_invited").mergeStyle(TextFormatting.RED), true);
                return 0;
            }
        }

        SkyblockInvitationEvent.Invite event = SkyblockHooks.onInvite(invitePlayer, team, player);
        switch (event.getResult()) {
            case DENY:
                source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.denied.invite_player").mergeStyle(TextFormatting.RED), true);
                return 0;
            case DEFAULT:
                if (!LibXConfigHandler.Utility.selfManage && !source.hasPermissionLevel(2)) {
                    source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.disabled.send_invitations").mergeStyle(TextFormatting.RED), true);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        data.addInvite(team, event.getInvitor(), invitePlayer);

        IFormattableTextComponent invite = new TranslationTextComponent("skyblockbuilder.command.info.invited_to_team0", player.getDisplayName().getString(), team.getName()).mergeStyle(TextFormatting.GOLD);
        invite.append(new StringTextComponent("/skyblock accept " + team.getName()).setStyle(Style.EMPTY
                .setHoverEvent(COPY_TEXT)
                .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skyblock accept " + team.getName()))
                .applyFormatting(TextFormatting.UNDERLINE).applyFormatting(TextFormatting.GOLD)));
        invite.append(new TranslationTextComponent("skyblockbuilder.command.info.invited_to_team1").mergeStyle(TextFormatting.GOLD));
        invitePlayer.sendStatusMessage(invite, false);

        return 1;
    }
}
