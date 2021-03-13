package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.melanx.skyblockbuilder.util.NameGenerator;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.IslandPos;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;

public class TeamCommand {

    public static final SuggestionProvider<CommandSource> SUGGEST_TEAMS = (context, builder) -> {
        return ISuggestionProvider.suggest(SkyblockSavedData.get(context.getSource().asPlayer().getServerWorld())
                .getTeams().stream().map(Team::getName).filter(name -> !name.equalsIgnoreCase("spawn")).collect(Collectors.toSet()), builder);
    };

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("teams").requires(source -> source.hasPermissionLevel(2))
                .then(Commands.literal("create")
                        .executes(context -> createTeam(context.getSource(), false))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> createTeam(context.getSource(), StringArgumentType.getString(context, "name"), false))))
                .then(Commands.literal("createAndJoin")
                        .executes(context -> createTeam(context.getSource(), true))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> createTeam(context.getSource(), StringArgumentType.getString(context, "name"), true))))
                .then(Commands.literal("join")
                        .then(Commands.argument("team", StringArgumentType.word()).suggests(SUGGEST_TEAMS)
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> addToTeam(context.getSource(), StringArgumentType.getString(context, "team"), EntityArgument.getPlayers(context, "players"))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> removeFromTeam(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("delete")
                        .then(Commands.argument("team", StringArgumentType.word()).suggests(SUGGEST_TEAMS)
                                .executes(context -> deleteTeam(context.getSource(), StringArgumentType.getString(context, "team")))))
                .then(Commands.literal("clear")
                        .executes(context -> deleteEmptyTeams(context.getSource()))
                        .then(Commands.argument("team", StringArgumentType.word()).suggests(SUGGEST_TEAMS)
                                .executes(context -> clearTeam(context.getSource(), StringArgumentType.getString(context, "team")))));
    }

    private static int deleteEmptyTeams(CommandSource source) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        int i = 0;
        Iterator<Team> itr = data.getTeams().iterator();
        while (itr.hasNext()) {
            Team team = itr.next();
            if (team.isEmpty()) {
                itr.remove();
                i++;
            }
        }
        data.markDirty();

        source.sendFeedback(new StringTextComponent(String.format("Deleted %s empty teams.", i)).mergeStyle(TextFormatting.GREEN), true);
        return 1;
    }

    private static int clearTeam(CommandSource source, String teamName) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (!data.teamExists(teamName)) {
            source.sendFeedback(new StringTextComponent("Team does not exist!").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        Team team = data.getTeam(teamName);
        assert team != null;
        int i = team.getPlayers().size();
        team.removeAllPlayers();
        source.sendFeedback(new StringTextComponent(String.format("Successfully removed all %s players.", i)), true);
        return 1;
    }

    private static int createTeam(CommandSource source, boolean join) {
        String team;
        Random rand = new Random();
        do {
            team = NameGenerator.randomName(rand);
        } while (SkyblockSavedData.get(source.getWorld()).teamExists(team));
        return createTeam(source, team, join);
    }

    private static int createTeam(CommandSource source, String name, boolean join) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (data.teamExists(name)) {
            source.sendFeedback(new StringTextComponent(String.format("Team %s already exists! Please choose another name!", name)).mergeStyle(TextFormatting.RED), true);
            return 0;
        }
        Team team = data.createTeam(name);


        if (join) {
            try {
                ServerPlayerEntity player = source.asPlayer();
                if (data.getTeamFromPlayer(player) != null) {
                    source.sendFeedback(new StringTextComponent("You are already in a team, to create a new one you have to leave your team first!").mergeStyle(TextFormatting.RED), false);
                    return 0;
                }

                //noinspection ConstantConditions
                team.addPlayer(player);
                WorldUtil.teleportToIsland(player, team.getIsland());
            } catch (CommandSyntaxException e) {
                source.sendFeedback(new StringTextComponent("You are no player, how do you want to join?"), false);
                return 1;
            }
        }

        source.sendFeedback(new StringTextComponent(String.format(("Successfully created team %s."), name)).mergeStyle(TextFormatting.GREEN), true);
        return 1;
    }

    private static int deleteTeam(CommandSource source, String team) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (!data.teamExists(team)) {
            source.sendFeedback(new StringTextComponent("Team does not exist!").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        //noinspection ConstantConditions
        Set<UUID> players = new HashSet<>(data.getTeam(team).getPlayers());
        if (!data.deleteTeam(team)) {
            source.sendFeedback(new StringTextComponent(String.format("Error while deleting team %s!", team)).mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        PlayerList playerList = source.getServer().getPlayerList();
        IslandPos spawn = data.getSpawn();
        players.forEach(id -> {
            ServerPlayerEntity player = playerList.getPlayerByUUID(id);
            if (player != null) {
                player.inventory.dropAllItems();
                WorldUtil.teleportToIsland(player, spawn);
            }
        });

        source.sendFeedback(new StringTextComponent(String.format("Successfully deleted team %s.", team)).mergeStyle(TextFormatting.GREEN), true);
        return 1;
    }

    private static int addToTeam(CommandSource source, String teamName, Collection<ServerPlayerEntity> players) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        if (!data.teamExists(teamName)) {
            source.sendFeedback(new StringTextComponent("Team does not exist!").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        IslandPos island = data.getTeamIsland(teamName);
        ServerPlayerEntity added = null;
        int i = 0;
        for (ServerPlayerEntity addedPlayer : players) {
            if (!data.hasPlayerTeam(addedPlayer)) {
                data.addPlayerToTeam(teamName, addedPlayer);
                WorldUtil.teleportToIsland(addedPlayer, island);
                if (i == 0) added = addedPlayer;
                i++;
            }
        }

        if (i == 0) {
            source.sendFeedback(new StringTextComponent("No player added to team!").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        if (i == 1)
            source.sendFeedback(new StringTextComponent(String.format("Successfully added %s to team %s.", added.getDisplayName().getString(), teamName)).mergeStyle(TextFormatting.GREEN), true);
        else
            source.sendFeedback(new StringTextComponent(String.format("Successfully added %s players to team %s.", i, teamName)).mergeStyle(TextFormatting.GREEN), true);
        return 1;
    }

    private static int removeFromTeam(CommandSource source, ServerPlayerEntity player) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            source.sendFeedback(new StringTextComponent("You're currently in no team!").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        String teamName = team.getName();
        data.removePlayerFromTeam(player);
        IslandPos spawn = data.getSpawn();
        player.inventory.dropAllItems();
        WorldUtil.teleportToIsland(player, spawn);
        source.sendFeedback(new StringTextComponent(String.format("Successfully left team %s.", teamName)).mergeStyle(TextFormatting.GREEN), true);
        return 1;
    }
}
