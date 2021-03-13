package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.server.ServerWorld;

public class InviteCommand {

    public static HoverEvent COPY_TEXT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to copy"));

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Invites the given player
        return Commands.literal("invite")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> invitePlayer(context.getSource(), EntityArgument.getPlayer(context, "player"))));
    }

    private static int invitePlayer(CommandSource source, ServerPlayerEntity invitePlayer) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);
        ServerPlayerEntity player = source.asPlayer();

        Team team = data.getTeamFromPlayer(player);
        if (team == null) {
            player.sendStatusMessage(new StringTextComponent("You're currently in no team!").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        Team invitedPlayersTeam = data.getTeamFromPlayer(invitePlayer);
        if (invitedPlayersTeam != null) {
            player.sendStatusMessage(new StringTextComponent("Invited player already is in a team!").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        if (data.hasInvites(invitePlayer)) {
            if (data.hasInviteFrom(team, invitePlayer)) {
                player.sendStatusMessage(new StringTextComponent("Player already invited to your team!").mergeStyle(TextFormatting.RED), false);
                return 0;
            }
        }

        data.addInvite(team, invitePlayer);

        IFormattableTextComponent invite = new StringTextComponent(String.format("%s invites you to join %s. Type ", player.getDisplayName().getString(), team.getName())).mergeStyle(TextFormatting.GOLD);
        invite.append(new StringTextComponent(String.format("/skyblock accept %s", team.getName()))
                .setStyle(Style.EMPTY
                        .setHoverEvent(COPY_TEXT)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skyblock accept " + team.getName()))
                        .applyFormatting(TextFormatting.UNDERLINE).applyFormatting(TextFormatting.GOLD)));
        invite.append(new StringTextComponent(" to join team.").mergeStyle(TextFormatting.GOLD));
        invitePlayer.sendStatusMessage(invite, false);

        player.sendStatusMessage(new StringTextComponent(String.format("Successfully invited %s in your team.", invitePlayer.getDisplayName().getString())).mergeStyle(TextFormatting.GOLD), false);
        return 1;
    }
}
