package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockJoinRequestEvent;
import de.melanx.skyblockbuilder.events.SkyblockManageTeamEvent;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.tuple.Pair;

public class TeamCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("team")
                // Let plays add/remove spawn points
                .then(Commands.literal("spawns")
                        .then(Commands.literal("add")
                                .executes(context -> addSpawn(context.getSource(), new BlockPos(context.getSource().getPosition())))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> addSpawn(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                        .then(Commands.literal("remove")
                                .executes(context -> removeSpawn(context.getSource(), new BlockPos(context.getSource().getPosition())))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos()).suggests(Suggestions.SPAWN_POSITIONS)
                                        .executes(context -> removeSpawn(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                        .then(Commands.literal("reset")
                                .executes(context -> resetSpawns(context.getSource(), null))
                                .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.ALL_TEAMS)
                                        .executes(context -> resetSpawns(context.getSource(), StringArgumentType.getString(context, "team"))))))

                // Renaming a team
                .then(Commands.literal("rename")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> renameTeam(context.getSource(), StringArgumentType.getString(context, "name"), null))
                                .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.ALL_TEAMS)
                                        .executes(context -> renameTeam(context.getSource(), StringArgumentType.getString(context, "name"), StringArgumentType.getString(context, "team"))))))

                // Toggle permission to visit the teams island
                .then(Commands.literal("allowVisit")
                        .executes(context -> showVisitInformation(context.getSource()))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> toggleAllowVisit(context.getSource(), BoolArgumentType.getBool(context, "enabled")))))

                // Toggle permission to send join requests to your team
                .then(Commands.literal("allowRequests")
                        .executes(context -> showRequestInformation(context.getSource()))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> toggleAllowRequest(context.getSource(), BoolArgumentType.getBool(context, "enabled")))))

                // Accept a join request
                .then(Commands.literal("accept")
                        .then(Commands.argument("player", EntityArgument.player()).suggests(Suggestions.INVITED_PLAYERS_OF_PLAYERS_TEAM)
                                .executes(context -> acceptRequest(context.getSource(), EntityArgument.getPlayer(context, "player")))))

                // Deny a join request
                .then(Commands.literal("deny")
                        .then(Commands.argument("player", EntityArgument.player()).suggests(Suggestions.INVITED_PLAYERS_OF_PLAYERS_TEAM)
                                .executes(context -> denyRequest(context.getSource(), EntityArgument.getPlayer(context, "player")))));
    }

    private static int acceptRequest(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        ServerPlayer commandPlayer = source.getPlayerOrException();
        Team team = data.getTeamFromPlayer(commandPlayer);
        if (team == null) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        if (data.hasPlayerTeam(player)) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.player_has_team"), false);
            team.removeJoinRequest(player);
            return 0;
        }

        SkyblockJoinRequestEvent.AcceptRequest event = SkyblockHooks.onAcceptJoinRequest(commandPlayer, player, team);
        switch (event.getResult()) {
            case DENY:
                source.sendSuccess(Component.translatable("skyblockbuilder.command.denied.accept_join_request").withStyle(ChatFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (!ConfigHandler.Utility.selfManage && !source.hasPermission(2)) {
                    source.sendSuccess(Component.translatable("skyblockbuilder.command.disabled.accept_join_request").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        team.broadcast(Component.translatable("skyblockbuilder.event.accept_join_request", commandPlayer.getDisplayName(), player.getDisplayName()), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        data.addPlayerToTeam(team, player);
        team.removeJoinRequest(player);
        WorldUtil.teleportToIsland(player, team);
        player.displayClientMessage(Component.translatable("skyblockbuilder.command.success.join_request_accepted", team.getName()).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int denyRequest(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        ServerPlayer commandPlayer = source.getPlayerOrException();
        Team team = data.getTeamFromPlayer(commandPlayer);
        if (team == null) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        if (data.hasPlayerTeam(player)) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.player_has_team"), false);
            team.removeJoinRequest(player);
            return 0;
        }

        SkyblockJoinRequestEvent.DenyRequest event = SkyblockHooks.onDenyJoinRequest(commandPlayer, player, team);
        switch (event.getResult()) {
            case DENY:
                source.sendSuccess(Component.translatable("skyblockbuilder.command.denied.deny_join_request").withStyle(ChatFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (!ConfigHandler.Utility.selfManage && !source.hasPermission(2)) {
                    source.sendSuccess(Component.translatable("skyblockbuilder.command.disabled.deny_join_request").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        team.broadcast(Component.translatable("skyblockbuilder.event.deny_join_request", commandPlayer.getDisplayName(), player.getDisplayName()), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        team.removeJoinRequest(player);
        player.displayClientMessage(Component.translatable("skyblockbuilder.command.success.deny_request_accepted", team.getName()).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int showVisitInformation(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        Team team = data.getTeamFromPlayer(source.getPlayerOrException());
        if (team == null) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        boolean enabled = team.allowsVisits();
        source.sendSuccess(Component.translatable("skyblockbuilder.command.info.visit_status", Component.translatable("skyblockbuilder.command.argument." + (enabled ? "enabled" : "disabled"))).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int toggleAllowVisit(CommandSourceStack source, boolean enabled) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        ServerPlayer player = source.getPlayerOrException();

        Team team = data.getTeamFromPlayer(player);
        if (team == null) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        Pair<Event.Result, Boolean> result = SkyblockHooks.onToggleVisits(player, team, enabled);
        if (result.getLeft() == Event.Result.DENY) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.denied.toggle_request", Component.translatable("skyblockbuilder.command.argument." + (enabled ? "enable" : "disable"))).withStyle(ChatFormatting.RED), false);
            return 0;
        } else {
            team.setAllowVisit(result.getRight());
            source.sendSuccess(Component.translatable("skyblockbuilder.command.info.toggle_visit", Component.translatable("skyblockbuilder.command.argument." + (enabled ? "enabled" : "disabled"))).withStyle(ChatFormatting.GOLD), false);
            return 1;
        }
    }

    private static int showRequestInformation(CommandSourceStack source) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        Team team = data.getTeamFromPlayer(source.getPlayerOrException());
        if (team == null) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        boolean enabled = team.allowsVisits();
        source.sendSuccess(Component.translatable("skyblockbuilder.command.info.visit_status", Component.translatable("skyblockbuilder.command.argument." + (enabled ? "enabled" : "disabled"))).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int toggleAllowRequest(CommandSourceStack source, boolean enabled) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        ServerPlayer player = source.getPlayerOrException();

        Team team = data.getTeamFromPlayer(player);
        if (team == null) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        Pair<Event.Result, Boolean> result = SkyblockHooks.onToggleRequests(player, team, enabled);
        if (result.getLeft() == Event.Result.DENY) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.denied.toggle_request", Component.translatable("skyblockbuilder.command.argument." + (enabled ? "enable" : "disable"))).withStyle(ChatFormatting.RED), false);
            return 0;
        } else {
            team.setAllowJoinRequest(result.getRight());
            source.sendSuccess(Component.translatable("skyblockbuilder.command.info.toggle_request", Component.translatable("skyblockbuilder.command.argument." + (enabled ? "enabled" : "disabled"))).withStyle(ChatFormatting.GOLD), false);
            return 1;
        }
    }

    private static int addSpawn(CommandSourceStack source, BlockPos pos) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        // check for overworld
        if (level != source.getServer().getLevel(ConfigHandler.Spawn.dimension)) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.wrong_position").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        ServerPlayer player = source.getPlayerOrException();
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            if (!source.hasPermission(2)) {
                source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
                return 0;
            }

            source.sendSuccess(Component.translatable("skyblockbuilder.command.warning.edit_spawn_spawns").withStyle(ChatFormatting.RED), false);
            team = data.getSpawn();
        }

        Pair<Event.Result, BlockPos> result = SkyblockHooks.onAddSpawn(player, team, pos);
        switch (result.getLeft()) {
            case DENY:
                source.sendSuccess(Component.translatable("skyblockbuilder.command.denied.create_spawn").withStyle(ChatFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (!ConfigHandler.Utility.Spawns.modifySpawns && !source.hasPermission(2)) {
                    source.sendSuccess(Component.translatable("skyblockbuilder.command.disabled.modify_spawns").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                Vec3i templateSize = TemplateData.get(level).getConfiguredTemplate().getTemplate().getSize();
                BlockPos center = team.getIsland().getCenter().mutable();
                center.offset(templateSize.getX() / 2, templateSize.getY() / 2, templateSize.getZ() / 2);
                if (!pos.closerThan(center, ConfigHandler.Utility.Spawns.range)) {
                    source.sendSuccess(Component.translatable("skyblockbuilder.command.error.position_too_far_away").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        team.addPossibleSpawn(pos);
        source.sendSuccess(Component.translatable("skyblockbuilder.command.success.spawn_added", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int removeSpawn(CommandSourceStack source, BlockPos pos) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        // check for overworld
        if (level != source.getServer().getLevel(ConfigHandler.Spawn.dimension)) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.wrong_position").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        ServerPlayer player = source.getPlayerOrException();
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        switch (SkyblockHooks.onRemoveSpawn(player, team, pos)) {
            case DENY:
                MutableComponent component = Component.translatable("skyblockbuilder.command.denied.modify_spawns0");
                if (team.getPossibleSpawns().size() <= 1) {
                    component.append(" ").append(Component.translatable("skyblockbuilder.command.denied.modify_spawns1"));
                }
                source.sendSuccess(component.withStyle(ChatFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (!ConfigHandler.Utility.Spawns.modifySpawns && !source.hasPermission(2)) {
                    source.sendSuccess(Component.translatable("skyblockbuilder.command.disabled.modify_spawns").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
            case ALLOW:
                break;
        }

        if (!team.removePossibleSpawn(pos)) {
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.remove_spawn0",
                    (team.getPossibleSpawns().size() <= 1
                            ? Component.literal(" ").append(Component.translatable("skyblockbuilder.command.error.remove_spawn1"))
                            : "")
            ).withStyle(ChatFormatting.RED), false);
            return 0;
        }

        source.sendSuccess(Component.translatable("skyblockbuilder.command.success.spawn_removed", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int resetSpawns(CommandSourceStack source, String name) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        Team team;

        ServerPlayer player = null;
        if (name == null) {
            if (!(source.getEntity() instanceof ServerPlayer)) {
                source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_no_player").withStyle(ChatFormatting.RED), false);
                return 0;
            }

            player = (ServerPlayer) source.getEntity();
            team = data.getTeamFromPlayer(player);

            if (team == null) {
                source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
                return 0;
            }
        } else {
            team = data.getTeam(name);

            if (team == null) {
                source.sendSuccess(Component.translatable("skyblockbuilder.command.error.team_not_exist").withStyle(ChatFormatting.RED), false);
                return 0;
            }
        }

        Event.Result result = SkyblockHooks.onResetSpawns(player, team);
        switch (result) {
            case DENY:
                source.sendSuccess(Component.translatable("skyblockbuilder.command.denied.reset_spawns").withStyle(ChatFormatting.GOLD), false);
                return 0;
            case DEFAULT:
                if (!ConfigHandler.Utility.Spawns.modifySpawns && !source.hasPermission(2)) {
                    source.sendSuccess(Component.translatable("skyblockbuilder.command.disabled.modify_spawns").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        team.setPossibleSpawns(team.getDefaultPossibleSpawns());
        source.sendSuccess(Component.translatable("skyblockbuilder.command.success.reset_spawns").withStyle(ChatFormatting.GOLD), true);
        return 1;
    }

    private static int renameTeam(CommandSourceStack source, String newName, String oldName) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        // Rename oldName to newName
        if (oldName != null) {
            Team team = data.getTeam(oldName);
            if (team == null) {
                source.sendSuccess(Component.translatable("skyblockbuilder.command.error.team_not_exist").withStyle(ChatFormatting.RED), false);
                return 0;
            }

            SkyblockManageTeamEvent.Rename event = SkyblockHooks.onRename(null, team, newName);
            switch (event.getResult()) {
                case DENY:
                    source.sendSuccess(Component.translatable("skyblockbuilder.command.error.denied_rename_team").withStyle(ChatFormatting.RED), false);
                    return 0;
                case DEFAULT:
                    if (!source.hasPermission(2)) {
                        source.sendSuccess(Component.translatable("skyblockbuilder.command.disabled.rename_team").withStyle(ChatFormatting.RED), false);
                        return 0;
                    }
                    break;
                case ALLOW:
                    break;
            }

            data.renameTeam(team, event.getPlayer(), event.getNewName());
        } else { // Get team from command user
            ServerPlayer player = source.getPlayerOrException();
            Team team = data.getTeamFromPlayer(player);

            if (team == null) {
                source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
                return 0;
            }

            SkyblockManageTeamEvent.Rename event = SkyblockHooks.onRename(player, team, newName);
            switch (event.getResult()) {
                case DENY:
                    source.sendSuccess(Component.translatable("skyblockbuilder.command.error.denied_rename_team").withStyle(ChatFormatting.RED), false);
                    return 0;
                case DEFAULT:
                case ALLOW:
                    break;
            }

            data.renameTeam(team, event.getPlayer(), event.getNewName());
        }

        source.sendSuccess(Component.translatable("skyblockbuilder.command.success.rename_team", newName).withStyle(ChatFormatting.GOLD), true);
        return 1;
    }
}
