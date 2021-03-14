package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.commands.operator.ManageCommand;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.Set;

public class TeamCommand {

    public static final SuggestionProvider<CommandSource> SUGGEST_POSITIONS = (context, builder) -> {
        Team team = SkyblockSavedData.get(context.getSource().getWorld()).getTeamFromPlayer(context.getSource().asPlayer());
        if (team != null) {
            Set<BlockPos> possibleSpawns = team.getPossibleSpawns();
            possibleSpawns.forEach(spawn -> {
                builder.suggest(String.format("%s %s %s", spawn.getX(), spawn.getY(), spawn.getZ()));
            });
        }

        return BlockPosArgument.blockPos().listSuggestions(context, builder);
    };

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("team")
                // Let plays add/remove spawn points
                .then(Commands.literal("spawns").requires(source -> ConfigHandler.modifySpawns.get())
                        .then(Commands.literal("add")
                                .executes(context -> addSpawn(context.getSource(), new BlockPos(context.getSource().getPos())))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> addSpawn(context.getSource(), BlockPosArgument.getBlockPos(context, "pos")))))
                        .then(Commands.literal("remove")
                                .executes(context -> removeSpawn(context.getSource(), new BlockPos(context.getSource().getPos())))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos()).suggests(SUGGEST_POSITIONS)
                                        .executes(context -> removeSpawn(context.getSource(), BlockPosArgument.getBlockPos(context, "pos")))))
                        .then(Commands.literal("reset")
                                .executes(context -> resetSpawns(context.getSource(), null))
                                .then(Commands.argument("team", StringArgumentType.word()).suggests(ManageCommand.SUGGEST_TEAMS)
                                        .executes(context -> resetSpawns(context.getSource(), StringArgumentType.getString(context, "team"))))))

                // Renaming a team
                .then(Commands.literal("rename")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> renameTeam(context.getSource(), StringArgumentType.getString(context, "name"), null))
                                .then(Commands.argument("team", StringArgumentType.word()).suggests(ManageCommand.SUGGEST_TEAMS).requires(source -> source.hasPermissionLevel(2))
                                        .executes(context -> renameTeam(context.getSource(), StringArgumentType.getString(context, "name"), StringArgumentType.getString(context, "team"))))));
    }

    private static int addSpawn(CommandSource source, BlockPos pos) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        // check for overworld
        if (world != source.getServer().func_241755_D_()) {
            source.sendFeedback(new StringTextComponent("Position needs to be in overworld.").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            source.sendFeedback(new StringTextComponent("Currently you aren't in a team.").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        BlockPos templateSize = TemplateLoader.TEMPLATE.getSize();
        BlockPos center = team.getIsland().getCenter().toMutable();
        center.add(templateSize.getX() / 2, templateSize.getY() / 2, templateSize.getZ() / 2);

        if (!pos.withinDistance(center, ConfigHandler.modifySpawnRange.get())) {
            source.sendFeedback(new StringTextComponent("This position is too far away from teams' island.").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        team.addPossibleSpawn(pos);
        source.sendFeedback(new StringTextComponent(String.format("Successfully added new spawn point at x %s, y %s, z %s", pos.getX(), pos.getY(), pos.getZ())).mergeStyle(TextFormatting.GOLD), false);
        return 1;
    }

    private static int removeSpawn(CommandSource source, BlockPos pos) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        // check for overworld
        if (world != source.getServer().func_241755_D_()) {
            source.sendFeedback(new StringTextComponent("Position needs to be in overworld.").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            source.sendFeedback(new StringTextComponent("Currently you aren't in a team.").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        if (!team.removePossibleSpawn(pos)) {
            source.sendFeedback(new StringTextComponent("You can't remove this spawn point. " + (team.getPossibleSpawns().size() <= 1 ? "There are too less spawn points left." : "")).mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        source.sendFeedback(new StringTextComponent(String.format("Successfully removed spawn point at x %s, y %s, z %s", pos.getX(), pos.getY(), pos.getZ())).mergeStyle(TextFormatting.GOLD), false);
        return 1;
    }

    private static int resetSpawns(CommandSource source, String name) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        Team team;

        if (name == null) {
            if (!(source.getEntity() instanceof ServerPlayerEntity)) {
                source.sendFeedback(new StringTextComponent("Which team? You aren't a player, you don't have a team!").mergeStyle(TextFormatting.RED), false);
                return 0;
            }

            ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
            team = data.getTeamFromPlayer(player);

            if (team == null) {
                source.sendFeedback(new StringTextComponent("Currently you are in no team.").mergeStyle(TextFormatting.RED), false);
                return 0;
            }
        } else {
            team = data.getTeam(name);

            if (team == null) {
                source.sendFeedback(new StringTextComponent("No valid team given.").mergeStyle(TextFormatting.RED), false);
                return 0;
            }
        }

        team.setPossibleSpawns(SkyblockSavedData.initialPossibleSpawns(team.getIsland().getCenter()));
        source.sendFeedback(new StringTextComponent("Successfully reset all possible spawns.").mergeStyle(TextFormatting.GOLD), false);
        return 1;
    }

    private static int renameTeam(CommandSource source, String newName, String oldName) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        // Rename oldName to newName
        if (oldName != null) {
            Team team = data.getTeam(oldName);
            if (team == null) {
                source.sendFeedback(new StringTextComponent("This team does not exist.").mergeStyle(TextFormatting.RED), true);
                return 0;
            }

            data.renameTeam(team, newName);
        } else { // Get team from command user
            Entity entity = source.getEntity();
            if (!(entity instanceof ServerPlayerEntity)) {
                source.sendFeedback(new StringTextComponent("You aren't a player, what's your team?").mergeStyle(TextFormatting.RED), true);
                return 0;
            }

            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            Team team = data.getTeamFromPlayer(player);

            if (team == null) {
                source.sendFeedback(new StringTextComponent("Currently you are in no team. Cannot change name of nothing.").mergeStyle(TextFormatting.RED), true);
                return 0;
            }

            data.renameTeam(team, newName);
        }

        source.sendFeedback(new StringTextComponent("Successfully renamed team to " + newName).mergeStyle(TextFormatting.GOLD), true);
        return 1;
    }
}
