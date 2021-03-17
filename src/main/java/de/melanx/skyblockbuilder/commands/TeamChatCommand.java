package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class TeamChatCommand {
    public static ArgumentBuilder<CommandSource, ?> register() {
        // Toggle team chat
        return Commands.literal("tc")
                .executes(context -> toggleTeamChat(context.getSource()))
                // Send a team message
                .then(Commands.argument("msg", MessageArgument.message())
                        .executes(context -> teamMessage(context.getSource(), MessageArgument.getMessage(context, "msg"))));
    }

    private static int toggleTeamChat(CommandSource source) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            source.sendFeedback(new StringTextComponent("Sad, you have no team to communicate with.").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        boolean enabled = team.toggleTeamChat(player);
        player.sendStatusMessage(new StringTextComponent("You're now in " + (enabled ? "team" : "normal") + " chat mode.").mergeStyle(TextFormatting.GOLD), true);
        return 1;
    }

    private static int teamMessage(CommandSource source, ITextComponent msg) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            source.sendFeedback(new StringTextComponent("Sad, you have no team to communicate with.").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        StringTextComponent tc = new StringTextComponent("<");
        tc.append(source.getDisplayName());
        tc.append(new StringTextComponent("> "));
        tc.append(new StringTextComponent(msg.getString()));
        team.broadcast(tc);
        return 1;
    }
}
