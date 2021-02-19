package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.melanx.skyblockbuilder.util.NameGenerator;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.world.IslandPos;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class TeamCommand {

    private static final SuggestionProvider<CommandSource> SUGGEST_TEAMS = (context, builder) -> {
        return ISuggestionProvider.suggest(SkyblockSavedData.get(context.getSource().asPlayer().getServerWorld())
                .getTeams().stream().map(Team::getName).filter(name -> !name.equalsIgnoreCase("spawn")).collect(Collectors.toSet()), builder);
    };

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("team")
                .then(Commands.literal("create")
                        .executes(context -> createTeam(context.getSource()))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> createTeam(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("join")
                        .then(Commands.argument("team", StringArgumentType.word()).suggests(SUGGEST_TEAMS)
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> addToTeam(context.getSource(), StringArgumentType.getString(context, "team"), EntityArgument.getPlayers(context, "players"))))))
                .then(Commands.literal("leave")
                        .then(Commands.argument("team", StringArgumentType.word()).suggests(SUGGEST_TEAMS)
                                .then(Commands.argument("player", EntityArgument.player()) // TODO only suggest players in team
                                        .executes(context -> removeFromTeam(context.getSource(), EntityArgument.getPlayer(context, "player"))))))
                .then(Commands.literal("delete")
                        .then(Commands.argument("team", StringArgumentType.word()).suggests(SUGGEST_TEAMS)
                                .executes(context -> deleteTeam(context.getSource(), StringArgumentType.getString(context, "team")))));
    }

    private static int createTeam(CommandSource source) {
        String team;
        Random rand = new Random();
        do {
            team = NameGenerator.randomName(rand);
        } while (SkyblockSavedData.get(source.getWorld()).teamExists(team));
        return createTeam(source, team);
    }

    private static int createTeam(CommandSource source, String name) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (data.teamExists(name)) {
            source.sendFeedback(new StringTextComponent("Team " + name + " already exists. Please choose another name."), false);
            return 0;
        }

        Team team = data.createTeam(name);
        //noinspection ConstantConditions
        IslandPos islandPos = team.getIsland();
        source.sendFeedback(new StringTextComponent("Successfully created team " + name), false);
        return 1;
    }

    private static int deleteTeam(CommandSource source, String team) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        ServerPlayerEntity player = source.asPlayer();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (!data.teamExists(team)) {
            player.sendStatusMessage(new StringTextComponent("Team does not exist").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        if (!data.deleteTeam(team)) {
            player.sendStatusMessage(new StringTextComponent("Error while deleting team " + team).mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        player.sendStatusMessage(new StringTextComponent("Successfully deleted team " + team).mergeStyle(TextFormatting.GREEN), false);
        return 1;
    }

    private static int addToTeam(CommandSource source, String team, Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        ServerPlayerEntity player = source.asPlayer();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (!data.teamExists(team)) {
            player.sendStatusMessage(new StringTextComponent("Team does not exist").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        IslandPos island = data.getTeamIsland(team);
        for (ServerPlayerEntity addedPlayer : players) {
            data.addPlayerToTeam(team, player);
            //noinspection ConstantConditions
            teleportToIsland(addedPlayer, island);
        }
        player.sendStatusMessage(new StringTextComponent(String.format("Successfully added %s to team %s", players.size() == 1 ? players.stream().findFirst().get().getDisplayName().getString() : players.size() + " players", team)).mergeStyle(TextFormatting.GREEN), false);
        return 1;
    }

    private static int removeFromTeam(CommandSource source, ServerPlayerEntity player) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            player.sendStatusMessage(new StringTextComponent("You're currently in no team").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        String teamName = team.getName();
        data.removePlayerFromTeam(player);
        IslandPos spawn = data.getSpawn();
        teleportToIsland(player, spawn);
        player.sendStatusMessage(new StringTextComponent("Successfully left team " + teamName).mergeStyle(TextFormatting.GREEN), false);
        return 1;
    }

    private static void teleportToIsland(ServerPlayerEntity player, IslandPos island) {
        ServerWorld world = player.getServerWorld();

        Set<BlockPos> possibleSpawns = SkyblockSavedData.getPossibleSpawns(island.getCenter());
        BlockPos spawn = new ArrayList<>(possibleSpawns).get(new Random().nextInt(possibleSpawns.size()));
        player.teleport(world, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
    }
}
