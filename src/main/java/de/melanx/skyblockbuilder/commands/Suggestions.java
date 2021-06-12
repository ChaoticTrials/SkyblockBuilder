package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Suggestions {

    // Lists all spawn positions of users team
    public static final SuggestionProvider<CommandSource> SPAWN_POSITIONS = (context, builder) -> {
        Team team = SkyblockSavedData.get(context.getSource().getWorld()).getTeamFromPlayer(context.getSource().asPlayer());
        if (team != null) {
            Set<BlockPos> possibleSpawns = team.getPossibleSpawns();
            possibleSpawns.forEach(spawn -> builder.suggest(String.format("%s %s %s", spawn.getX(), spawn.getY(), spawn.getZ())));
        }

        return BlockPosArgument.blockPos().listSuggestions(context, builder);
    };

    // Lists all players invited by players team
    public static final SuggestionProvider<CommandSource> INVITED_PLAYERS_OF_PLAYERS_TEAM = (context, builder) -> {
        Team team = SkyblockSavedData.get(context.getSource().getWorld()).getTeamFromPlayer(context.getSource().asPlayer());
        if (team != null) {
            Set<UUID> players = team.getJoinRequests();
            PlayerList playerList = context.getSource().getServer().getPlayerList();
            players.forEach(id -> {
                ServerPlayerEntity player = playerList.getPlayerByUUID(id);
                if (player != null) {
                    builder.suggest(player.getDisplayName().getString());
                }
            });
        }

        return EntityArgument.entity().listSuggestions(context, builder);
    };

    // Lists all templates
    public static final SuggestionProvider<CommandSource> TEMPLATES = ((context, builder) -> ISuggestionProvider.suggest(TemplateLoader.getTemplates().keySet(), builder));

    // Lists all teams except spawn
    public static final SuggestionProvider<CommandSource> ALL_TEAMS = (context, builder) -> ISuggestionProvider.suggest(SkyblockSavedData.get(context.getSource().asPlayer().getServerWorld())
            .getTeams().stream()
            .filter(team -> !team.isSpawn())
            .map(Team::getName)
            .collect(Collectors.toSet()), builder);

    // Lists all teams which allow visiting
    public static final SuggestionProvider<CommandSource> VISIT_TEAMS = (context, builder) -> ISuggestionProvider.suggest(SkyblockSavedData.get(context.getSource().asPlayer().getServerWorld())
            .getTeams().stream()
            .filter(Team::allowsVisits)
            .filter(team -> !team.isSpawn())
            .map(Team::getName)
            .collect(Collectors.toSet()), builder);

    // Lists all teams for a player which invited the player
    public static final SuggestionProvider<CommandSource> INVITE_TEAMS = (context, builder) -> {
        CommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        List<Team> teams = data.getInvites(source.asPlayer());
        if (teams != null && teams.size() != 0) {
            return ISuggestionProvider.suggest(teams.stream()
                    .filter(team -> !team.isSpawn())
                    .map(Team::getName)
                    .collect(Collectors.toSet()), builder);
        }

        return ISuggestionProvider.suggest(new String[]{""}, builder);
    };
}
