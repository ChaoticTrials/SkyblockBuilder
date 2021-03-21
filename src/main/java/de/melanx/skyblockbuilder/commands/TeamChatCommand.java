package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.tuple.Pair;

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
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.user_has_no_team_teamchat").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        boolean mode = !team.isInTeamChat(player);
        Pair<Boolean, Event.Result> result = SkyblockHooks.onTeamChatChange(player, team, mode);
        if (result.getLeft()) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.denied.team_chat").mergeStyle(TextFormatting.RED), true);
            return 0;
        } else switch (result.getRight()) {
            case DENY:
                mode = false;
                break;
            case DEFAULT:
                break;
            case ALLOW:
                mode = true;
                break;
        }

        team.setTeamChat(player, mode);
        player.sendStatusMessage(new TranslationTextComponent("skyblockbuilder.command.success.toggle_teamchat", new TranslationTextComponent("skyblockbuilder.command.argument." + (mode ? "team" : "normal"))).mergeStyle(TextFormatting.GOLD), true);
        return 1;
    }

    private static int teamMessage(CommandSource source, ITextComponent msg) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.user_has_no_team_teamchat").mergeStyle(TextFormatting.RED), true);
            return 0;
        }
        
        ITextComponent component = SkyblockHooks.onTeamChat(player, team, new StringTextComponent(msg.getString()));

        if (component != null) {
            StringTextComponent tc = new StringTextComponent("<");
            tc.append(source.getDisplayName());
            tc.append(new StringTextComponent("> "));
            tc.append(component);
            team.broadcast(tc);
        }
        return 1;
    }
}
