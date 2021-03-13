package de.melanx.skyblockbuilder.commands.invitation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.stream.Collectors;

public class AcceptCommand {

    // Lists all teams except spawn
    private static final SuggestionProvider<CommandSource> SUGGEST_TEAMS = (context, builder) -> {
        CommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        List<Team> teams = data.getInvites(source.asPlayer());
        if (teams != null && teams.size() != 0) {
            return ISuggestionProvider.suggest(teams.stream()
                    .map(Team::getName).filter(name -> !name.equalsIgnoreCase("spawn")).collect(Collectors.toSet()), builder);
        }
        return ISuggestionProvider.suggest(new String[]{""}, builder);
    };

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Accepts an invitation
        return Commands.literal("accept")
                .then(Commands.argument("team", StringArgumentType.word()).suggests(SUGGEST_TEAMS)
                        .executes(context -> acceptTeam(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int acceptTeam(CommandSource source, String teamName) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);
        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeam(teamName);

        if (team == null) {
            source.sendFeedback(new StringTextComponent("This team does not exist!").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        if (data.hasPlayerTeam(player)) {
            source.sendFeedback(new StringTextComponent("You already joined a team!").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        if (!data.hasInvites(player)) {
            source.sendFeedback(new StringTextComponent("You don't have any invitations!").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        if (!data.acceptInvite(team, player)) {
            source.sendFeedback(new StringTextComponent("Error while accepting the invitation.").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        source.sendFeedback(new StringTextComponent(String.format("Successfully joined team %s.", team.getName())).mergeStyle(TextFormatting.GOLD), false);
        return 1;
    }

}
