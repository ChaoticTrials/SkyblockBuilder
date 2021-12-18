package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Suggestions {

    // Lists all spawn positions of users team
    public static final SuggestionProvider<CommandSourceStack> SPAWN_POSITIONS = (context, builder) -> {
        Team team = SkyblockSavedData.get(context.getSource().getLevel()).getTeamFromPlayer(context.getSource().getPlayerOrException());
        if (team != null) {
            Set<BlockPos> possibleSpawns = team.getPossibleSpawns();
            possibleSpawns.forEach(spawn -> builder.suggest(String.format("%s %s %s", spawn.getX(), spawn.getY(), spawn.getZ())));
        }

        return BlockPosArgument.blockPos().listSuggestions(context, builder);
    };

    // Lists all players invited by players team
    public static final SuggestionProvider<CommandSourceStack> INVITED_PLAYERS_OF_PLAYERS_TEAM = (context, builder) -> {
        Team team = SkyblockSavedData.get(context.getSource().getLevel()).getTeamFromPlayer(context.getSource().getPlayerOrException());
        if (team != null) {
            Set<UUID> players = team.getJoinRequests();
            PlayerList playerList = context.getSource().getServer().getPlayerList();
            players.forEach(id -> {
                ServerPlayer player = playerList.getPlayer(id);
                if (player != null) {
                    builder.suggest(player.getDisplayName().getString());
                }
            });
        }

        return EntityArgument.entity().listSuggestions(context, builder);
    };

    // Lists all templates
    public static final SuggestionProvider<CommandSourceStack> TEMPLATES = ((context, builder) -> SharedSuggestionProvider
            .suggest(TemplateLoader.getTemplateNames().stream()
                    .map(s -> s.split(" ").length == 1 ? s : "\"" + s + "\""), builder));

    // Lists all teams except spawn
    public static final SuggestionProvider<CommandSourceStack> ALL_TEAMS = (context, builder) -> SharedSuggestionProvider
            .suggest(SkyblockSavedData.get(context.getSource().getPlayerOrException().getLevel())
                    .getTeams().stream()
                    .filter(team -> !team.isSpawn())
                    .map(team -> team.getName().split(" ").length == 1 ? team.getName() : "\"" + team.getName() + "\"")
                    .collect(Collectors.toSet()), builder);

    // Lists all teams which allow visiting
    public static final SuggestionProvider<CommandSourceStack> VISIT_TEAMS = (context, builder) -> SharedSuggestionProvider
            .suggest(SkyblockSavedData.get(context.getSource().getPlayerOrException().getLevel())
                    .getTeams().stream()
                    .filter(Team::allowsVisits)
                    .filter(team -> !team.isSpawn())
                    .map(team -> team.getName().split(" ").length == 1 ? team.getName() : "\"" + team.getName() + "\"")
                    .collect(Collectors.toSet()), builder);

    // Lists all teams for a player which invited the player
    public static final SuggestionProvider<CommandSourceStack> INVITE_TEAMS = (context, builder) -> {
        CommandSourceStack source = context.getSource();
        ServerLevel world = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        List<UUID> teams = data.getInvites(source.getPlayerOrException());
        if (teams != null && teams.size() != 0) {
            return SharedSuggestionProvider.suggest(teams.stream()
                    .map(data::getTeam)
                    .filter(Objects::nonNull)
                    .filter(team -> !team.isSpawn())
                    .map(team -> team.getName().split(" ").length == 1 ? team.getName() : "\"" + team.getName() + "\"")
                    .collect(Collectors.toSet()), builder);
        }

        return SharedSuggestionProvider.suggest(new String[]{""}, builder);
    };
}
