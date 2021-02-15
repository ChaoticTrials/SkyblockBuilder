package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.melanx.skyblockbuilder.EventListener;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.world.IslandPos;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class TeamCommand {

    private static final SuggestionProvider<CommandSource> SUGGEST_TEAMS = (context, builder) -> {
        return ISuggestionProvider.suggest(SkyblockSavedData.get(context.getSource().asPlayer().getServerWorld())
                        .getTeams().stream().map(Team::getName).collect(Collectors.toSet()), builder);
    };

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("team")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> createTeam(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("join")
                        .then(Commands.argument("team", StringArgumentType.word()).suggests(SUGGEST_TEAMS)
                                .executes(context -> joinTeam(context.getSource(), StringArgumentType.getString(context, "team")))))
                .then(Commands.literal("leave")
                        .executes(context -> leaveTeam(context.getSource()))); // fixme
    }

    private static int createTeam(CommandSource source, String name) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);
        ServerPlayerEntity player = source.asPlayer();
        name = name.toLowerCase();

        if (data.hasPlayerTeam(player)) {
            player.sendStatusMessage(new StringTextComponent("You're already in a team."), false);
            return 0;
        }

        if (data.teamExists(name)) {
            player.sendStatusMessage(new StringTextComponent("Team " + name + " already exists. Please choose another name."), false);
            return 0;
        }

        Team team = data.createTeamAndJoin(name, player);
        //noinspection ConstantConditions
        IslandPos islandPos = team.getIsland();
        EventListener.spawnPlayer(player, islandPos);
        player.sendStatusMessage(new StringTextComponent("Successfully created and joined team " + name), false);
        return 1;
    }

    private static int joinTeam(CommandSource source, String team) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        ServerPlayerEntity player = source.asPlayer();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (!data.teamExists(team)) {
            player.sendStatusMessage(new StringTextComponent("Team does not exist").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        if (data.hasPlayerTeam(player)) {
            player.sendStatusMessage(new StringTextComponent("You're already in a team").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        data.addPlayerToTeam(team, player);
        //noinspection ConstantConditions
        teleportToIsland(source, data.getTeamIsland(team));
        player.sendStatusMessage(new StringTextComponent("Successfully joined team " + team).mergeStyle(TextFormatting.GREEN), false);
        return 1;
    }

    private static int leaveTeam(CommandSource source) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        ServerPlayerEntity player = source.asPlayer();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            player.sendStatusMessage(new StringTextComponent("You're currently in no team").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        String teamName = team.getName();
        data.removePlayerFromTeam(player);
        IslandPos spawn = data.getSpawn();
        teleportToIsland(source, spawn);
        player.sendStatusMessage(new StringTextComponent("Successfully left team " + teamName).mergeStyle(TextFormatting.GREEN), false);
        return 1;
    }

    private static void teleportToIsland(CommandSource source, IslandPos island) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        ServerPlayerEntity player = source.asPlayer();

        Set<BlockPos> possibleSpawns = SkyblockSavedData.getPossibleSpawns(island.getCenter());
        BlockPos spawn = new ArrayList<>(possibleSpawns).get(new Random().nextInt(possibleSpawns.size()));
        player.teleport(world, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
    }
}
