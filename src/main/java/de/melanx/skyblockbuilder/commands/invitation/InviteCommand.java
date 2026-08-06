package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockInvitationEvent;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class InviteCommand {

    public static final HoverEvent COPY_TEXT = new HoverEvent.ShowText(Component.translatable("chat.copy.click"));

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
            source.sendFailure(SkyComponents.ERROR_USER_HAS_NO_TEAM);
            return 0;
        }

        Team invitedPlayersTeam = data.getTeamFromPlayer(invitePlayer);
        if (invitedPlayersTeam != null) {
            source.sendFailure(SkyComponents.ERROR_PLAYER_HAS_TEAM.apply(invitePlayer.getName().getString()));
            return 0;
        }

        if (data.hasInviteFrom(team, invitePlayer)) {
            source.sendFailure(SkyComponents.ERROR_PLAYER_ALREADY_INVITED);
            return 0;
        }

        SkyblockInvitationEvent.Invite event = SkyblockHooks.onInvite(invitePlayer, team, player);
        switch (event.getResult()) {
            case DENY:
                source.sendFailure(SkyComponents.DENIED_INVITE_PLAYER);
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TEAM_HANDLE_INVITES)) {
                    source.sendFailure(SkyComponents.DISABLED_SEND_INVITATIONS);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        data.addInvite(team, event.getInvitor(), invitePlayer);

        MutableComponent invite = SkyComponents.INFO_INVITED_TO_TEAM0.apply(player.getDisplayName().getString(), team.getName()).withStyle(ChatFormatting.GOLD);
        invite.append(Component.literal("/skyblock accept \"" + team.getName() + "\"").setStyle(Style.EMPTY
                .withHoverEvent(COPY_TEXT)
                .withClickEvent(new ClickEvent.SuggestCommand("/skyblock accept \"" + team.getName() + "\""))
                .applyFormat(ChatFormatting.UNDERLINE).applyFormat(ChatFormatting.GOLD)));
        invite.append(SkyComponents.INFO_INVITED_TO_TEAM1.withStyle(ChatFormatting.GOLD));
        invitePlayer.sendSystemMessage(invite, false);

        return 1;
    }
}
