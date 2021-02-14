package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.melanx.skyblockbuilder.world.data.Teams;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class TeamCommand {

    private static final SuggestionProvider<CommandSource> SUGGEST_TEAMS = (context, builder) -> {
        return ISuggestionProvider.suggest(Teams.get(context.getSource().asPlayer().getServerWorld()).getTeams(), builder);
    };

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("team")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> createTeam(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("join")
                        .then(Commands.argument("team", StringArgumentType.word()).suggests(SUGGEST_TEAMS).executes(context -> {
                            return joinTeam(context.getSource(), StringArgumentType.getString(context, "team"));
                        })));
    }

    private static int createTeam(CommandSource source, String name) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        Teams teams = Teams.get(world);
        ServerPlayerEntity player = source.asPlayer();

        if (teams.hasTeam(player)) {
            player.sendStatusMessage(new StringTextComponent("You're already in a team."), false);
            return 0;
        }

        if (teams.exists(name)) {
            player.sendStatusMessage(new StringTextComponent("Team " + name + " already exists. Please choose another name."), false);
            return 0;
        }

        teams.add(name, player.getUniqueID());
        player.sendStatusMessage(new StringTextComponent("Successfully created and joined team " + name), false);
        return 1;
    }

    private static int joinTeam(CommandSource source, String team) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        ServerPlayerEntity player = source.asPlayer();
        Teams teams = Teams.get(world);

        if (!teams.exists(team)) {
            player.sendStatusMessage(new StringTextComponent("Team does not exist.").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        if (teams.hasTeam(player)) {
            player.sendStatusMessage(new StringTextComponent("You're already in a team.").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        teams.add(team, player.getUniqueID()); // todo teleport player to new island
        player.sendStatusMessage(new StringTextComponent("Successfully joined team " + team).mergeStyle(TextFormatting.GREEN), false);
        return 1;
    }
}
