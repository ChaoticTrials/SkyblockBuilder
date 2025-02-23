package de.melanx.skyblockbuilder.commands.operator;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.config.common.InventoryConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.NameGenerator;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class ManageCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("manage").requires(PermissionManager.INSTANCE::mayExecuteOpCommand)
                // refreshes the island shape
                .then(Commands.literal("islandShape")
                        .then(Commands.argument("template", StringArgumentType.string()).suggests(Suggestions.TEMPLATES)
                                .executes(context -> refreshIsland(context.getSource(), StringArgumentType.getString(context, "template")))))

                .then(Commands.literal("teams")
                        // Removes all empty teams in the world
                        .then(Commands.literal("clear")
                                .executes(context -> deleteEmptyTeams(context.getSource()))
                                .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.ALL_TEAMS)
                                        .executes(context -> clearTeam(context.getSource(), StringArgumentType.getString(context, "team")))))

                        // Creates a team
                        .then(Commands.literal("create")
                                .executes(context -> createTeam(context.getSource(), false))
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(context -> createTeam(context.getSource(), StringArgumentType.getString(context, "name"), false))))

                        // Creates a team and the player executing the command joins
                        .then(Commands.literal("createAndJoin")
                                .executes(context -> createTeam(context.getSource(), true))
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(context -> createTeam(context.getSource(), StringArgumentType.getString(context, "name"), true))))

                        // Deletes the team with the given name
                        .then(Commands.literal("delete")
                                .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.ALL_TEAMS)
                                        .executes(context -> deleteTeam(context.getSource(), StringArgumentType.getString(context, "team"))))))

                // Adds player(s) to a given team
                .then(Commands.literal("addPlayer")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.ALL_TEAMS)
                                        .executes(context -> addToTeam(context.getSource(), StringArgumentType.getString(context, "team"), EntityArgument.getPlayers(context, "players"))))))
                // Kicks player from its current team
                .then(Commands.literal("kickPlayer")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> removeFromTeam(context.getSource(), EntityArgument.getPlayer(context, "player")))));
    }

    private static int refreshIsland(CommandSourceStack source, String name) {
        TemplateLoader.setTemplate(TemplateLoader.getConfiguredTemplate(name));
        TemplateData.get(source.getLevel()).refreshTemplate();
        source.sendSuccess(() -> SkyComponents.SUCCESS_RESET_ISLAND.apply(name), true);

        return 1;
    }

    private static int deleteEmptyTeams(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        int i = 0;
        Iterator<Team> itr = data.getTeams().iterator();
        while (itr.hasNext()) {
            Team team = itr.next();
            if (!SkyblockHooks.onManageDeleteTeam(source, team)) {
                if (team.isEmpty()) {
                    itr.remove();
                    i++;
                }
            }
        }
        data.setDirty();

        int teamsAmount = i;
        source.sendSuccess(() -> SkyComponents.SUCCESS_DELETE_MULTIPLE_TEAMS.apply(teamsAmount), true);
        return 1;
    }

    private static int clearTeam(CommandSourceStack source, String teamName) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        Team team = data.getTeam(teamName);
        if (team == null) {
            source.sendFailure(SkyComponents.ERROR_TEAM_NOT_EXIST);
            return 0;
        }

        if (SkyblockHooks.onManageClearTeam(source, team)) {
            source.sendFailure(SkyComponents.DENIED_CLEAR_TEAM);
            return 0;
        }

        int i = team.getPlayers().size();
        data.removeAllPlayersFromTeam(team);
        RandomUtility.deleteTeamIfEmpty(data, team);
        source.sendSuccess(() -> SkyComponents.SUCCESS_REMOVE_ALL_PLAYERS_FROM_TEAM.apply(i), true);
        return 1;
    }

    private static int createTeam(CommandSourceStack source, boolean join) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        String team;
        Random rand = new Random();
        do {
            team = NameGenerator.randomName(rand);
        } while (SkyblockSavedData.get(source.getLevel()).teamExists(team));
        return createTeam(source, team, join);
    }

    private static int createTeam(CommandSourceStack source, String name, boolean join) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        Pair<Boolean, String> result = SkyblockHooks.onManageCreateTeam(source, name, join);
        if (result.getLeft()) {
            source.sendFailure(SkyComponents.DENIED_CREATE_TEAM);
            return 0;
        }

        Team team = data.createTeam(result.getRight());
        if (team == null) {
            source.sendFailure(SkyComponents.ERROR_TEAM_ALREADY_EXIST.apply(result.getRight()));
            return 0;
        }

        if (join) {
            try {
                ServerPlayer player = source.getPlayerOrException();
                if (data.getTeamFromPlayer(player) != null) {
                    source.sendFailure(SkyComponents.ERROR_USER_HAS_TEAM);
                    return 0;
                }

                data.addPlayerToTeam(team, player);
                WorldUtil.teleportToIsland(player, team);
            } catch (CommandSyntaxException e) {
                source.sendFailure(SkyComponents.ERROR_USER_NO_PLAYER);
                return 1;
            }
        }

        source.sendSuccess(() -> SkyComponents.SUCCESS_CREATE_TEAM.apply(result.getRight()), true);
        return 1;
    }

    private static int deleteTeam(CommandSourceStack source, String team) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        if (!data.teamExists(team)) {
            source.sendFailure(SkyComponents.ERROR_TEAM_NOT_EXIST);
            return 0;
        }

        if (SkyblockHooks.onManageDeleteTeam(source, data.getTeam(team))) {
            source.sendFailure(SkyComponents.DENIED_DELETE_TEAM);
            return 0;
        }

        //noinspection ConstantConditions
        Set<UUID> players = new HashSet<>(data.getTeam(team).getPlayers());
        if (!data.deleteTeam(team)) {
            source.sendFailure(SkyComponents.ERROR_DELETE_TEAM.apply(team));
            return 0;
        }

        PlayerList playerList = source.getServer().getPlayerList();
        Team spawn = data.getSpawn();
        players.forEach(id -> {
            ServerPlayer player = playerList.getPlayer(id);
            if (player != null) {
                if (InventoryConfig.dropItems) {
                    RandomUtility.dropInventories(player);
                }
                WorldUtil.teleportToIsland(player, spawn);
            }
        });

        source.sendSuccess(() -> SkyComponents.SUCCESS_DELETE_ONE_TEAM.apply(team), true);
        return 1;
    }

    private static int addToTeam(CommandSourceStack source, String teamName, Collection<ServerPlayer> players) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        if (!data.teamExists(teamName)) {
            source.sendFailure(SkyComponents.ERROR_TEAM_NOT_EXIST);
            return 0;
        }

        Team island = data.getTeam(teamName);
        Pair<Boolean, Set<ServerPlayer>> result = SkyblockHooks.onManageAddToTeam(source, island, players);
        if (result.getLeft()) {
            source.sendFailure(SkyComponents.DENIED_ADD_PLAYERS_TO_TEAM);
            return 0;
        }

        ServerPlayer added = null;
        int i = 0;
        for (ServerPlayer addedPlayer : result.getRight()) {
            if (!data.hasPlayerTeam(addedPlayer)) {
                data.addPlayerToTeam(teamName, addedPlayer);
                WorldUtil.teleportToIsland(addedPlayer, island);
                if (i == 0) added = addedPlayer;
                i += 1;
            }
        }

        if (i == 0) {
            source.sendFailure(SkyComponents.ERROR_NO_PLAYER_ADDED);
            return 0;
        }

        if (i == 1) {
            String playerName = added.getDisplayName().getString();
            source.sendSuccess(() -> SkyComponents.SUCCESS_ADD_ONE_PLAYER.apply(playerName, teamName), true);
        } else {
            int playerAmount = i;
            source.sendSuccess(() -> SkyComponents.SUCCESS_ADD_MULTIPLE_PLAYERS.apply(playerAmount, teamName), true);
        }

        return 1;
    }

    private static int removeFromTeam(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            source.sendFailure(SkyComponents.ERROR_PLAYER_HAS_NO_TEAM);
            return 0;
        }

        Pair<Boolean, Set<ServerPlayer>> result = SkyblockHooks.onManageRemoveFromTeam(source, team, ImmutableSet.of(player));
        if (result.getLeft()) {
            source.sendFailure(SkyComponents.DENIED_REMOVE_PLAYERS_FROM_TEAM);
            return 0;
        }

        String teamName = team.getName();
        Team spawn = data.getSpawn();
        int i = 0;
        for (ServerPlayer target : result.getRight()) {
            // Even if the event adds players to the list
            // we only remove the ones that really are in the team.
            if (team.hasPlayer(target)) {
                data.removePlayerFromTeam(target);
                if (InventoryConfig.dropItems) {
                    RandomUtility.dropInventories(target);
                }
                WorldUtil.teleportToIsland(target, spawn);
                i += 1;
            }
        }
        RandomUtility.deleteTeamIfEmpty(data, team);
        int playerAmount = i;
        source.sendSuccess(() -> SkyComponents.SUCCESS_REMOVE_MULTIPLE_PLAYERS.apply(playerAmount, teamName), true);
        return 1;
    }
}
