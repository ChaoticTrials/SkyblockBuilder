package de.melanx.skyblockbuilder.commands.team;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.commands.Suggestions;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.config.common.SpawnConfig;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.events.SkyblockManageTeamEvent;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

public class EditTeamSpawnsCommand {

    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("spawns")
                .then(Commands.literal("add")
                        .executes(context -> EditTeamSpawnsCommand.addSpawn(context.getSource(), BlockPos.containing(context.getSource().getPosition())))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> EditTeamSpawnsCommand.addSpawn(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("remove")
                        .executes(context -> EditTeamSpawnsCommand.removeSpawn(context.getSource(), BlockPos.containing(context.getSource().getPosition())))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos()).suggests(Suggestions.SPAWN_POSITIONS)
                                .executes(context -> EditTeamSpawnsCommand.removeSpawn(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("reset")
                        .executes(context -> EditTeamSpawnsCommand.resetSpawns(context.getSource(), null))
                        .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.ALL_TEAMS)
                                .executes(context -> EditTeamSpawnsCommand.resetSpawns(context.getSource(), StringArgumentType.getString(context, "team")))));
    }

    private static int addSpawn(CommandSourceStack source, BlockPos pos) throws CommandSyntaxException {
        ValidationResult validationResult = EditTeamSpawnsCommand.validateTeamSpawnLocation(source);

        if (validationResult == null) {
            return 0;
        }

        ServerPlayer player = validationResult.player();
        Team team = validationResult.team();

        if (team == null) {
            if (!PermissionManager.INSTANCE.mayExecuteOpCommand(player)) {
                source.sendFailure(SkyComponents.ERROR_USER_HAS_NO_TEAM);
                return 0;
            }

            source.sendFailure(SkyComponents.WARNING_EDIT_SPAWN_SPAWNS);
            team = SkyblockSavedData.get(player.level()).getSpawn();
        }

        Pair<SkyblockManageTeamEvent.Result, TemplatesConfig.Spawn> result = SkyblockHooks.onAddSpawn(player, team, pos, player.getDirection());
        switch (result.getLeft()) {
            case DENY:
                source.sendFailure(SkyComponents.DENIED_CREATE_SPAWN);
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.EDIT_SPAWNS)) {
                    source.sendFailure(SkyComponents.DISABLED_MODIFY_SPAWNS);
                    return 0;
                }

                Vec3i templateSize = TemplateData.get((ServerLevel) player.level()).getConfiguredTemplate().getTemplate().getSize();
                BlockPos center = team.getIsland().getCenter().mutable();
                center.offset(templateSize.getX() / 2, templateSize.getY() / 2, templateSize.getZ() / 2);
                if (!result.getValue().pos().closerThan(center, PermissionsConfig.Spawns.range)) {
                    source.sendFailure(SkyComponents.ERROR_POSITION_TOO_FAR_AWAY);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        team.addPossibleSpawn(result.getValue());
        source.sendSuccess(() -> SkyComponents.SUCCESS_SPAWN_ADDED.apply(pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int removeSpawn(CommandSourceStack source, BlockPos pos) throws CommandSyntaxException {
        ValidationResult validationResult = validateTeamSpawnLocation(source);
        if (validationResult == null) {
            return 0;
        }

        if (validationResult.team() == null) {
            source.sendFailure(SkyComponents.ERROR_USER_HAS_NO_TEAM);
            return 0;
        }

        switch (SkyblockHooks.onRemoveSpawn(validationResult.player(), validationResult.team(), pos)) {
            case DENY:
                MutableComponent component = SkyComponents.DENIED_MODIFY_SPAWNS0;
                if (validationResult.team().getPossibleSpawns().size() <= 1) {
                    component.append(" ").append(SkyComponents.DENIED_MODIFY_SPAWNS1);
                }

                source.sendFailure(component);
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(validationResult.player(), PermissionManager.Permission.EDIT_SPAWNS)) {
                    source.sendFailure(SkyComponents.DISABLED_MODIFY_SPAWNS);
                    return 0;
                }
            case ALLOW:
                break;
        }

        if (!validationResult.team().removePossibleSpawn(pos)) {
            MutableComponent component = SkyComponents.ERROR_REMOVE_SPAWN0;
            if (validationResult.team().getPossibleSpawns().size() <= 1) {
                component.append(" ").append(SkyComponents.ERROR_REMOVE_SPAWN1);
            }

            source.sendFailure(component);
            return 0;
        }

        source.sendSuccess(() -> SkyComponents.SUCCESS_SPAWN_REMOVED.apply(pos.getX(), pos.getY(), pos.getZ()), false);
        return 1;
    }

    @Nullable
    private static ValidationResult validateTeamSpawnLocation(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        if (level != source.getServer().getLevel(SpawnConfig.spawmDimension)) {
            source.sendFailure(SkyComponents.ERROR_WRONG_POSITION);
            return null;
        }

        ServerPlayer player = source.getPlayerOrException();
        Team team = data.getTeamFromPlayer(player);
        return new ValidationResult(player, team);
    }

    private record ValidationResult(ServerPlayer player, Team team) {}

    private static int resetSpawns(CommandSourceStack source, String name) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        Team team;

        ServerPlayer player = null;
        if (name == null) {
            if (!(source.getEntity() instanceof ServerPlayer)) {
                source.sendFailure(SkyComponents.ERROR_USER_NO_PLAYER);
                return 0;
            }

            player = (ServerPlayer) source.getEntity();
            team = data.getTeamFromPlayer(player);

            if (team == null) {
                source.sendFailure(SkyComponents.ERROR_USER_HAS_NO_TEAM);
                return 0;
            }
        } else {
            team = data.getTeam(name);

            if (team == null) {
                source.sendFailure(SkyComponents.ERROR_TEAM_NOT_EXIST);
                return 0;
            }
        }

        SkyblockManageTeamEvent.Result result = SkyblockHooks.onResetSpawns(player, team);
        switch (result) {
            case DENY:
                source.sendFailure(SkyComponents.DENIED_RESET_SPAWNS);
                return 0;
            case DEFAULT:
                if (!PermissionManager.INSTANCE.hasPermission(source, PermissionManager.Permission.EDIT_SPAWNS)) {
                    source.sendFailure(SkyComponents.DISABLED_MODIFY_SPAWNS);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        team.setPossibleSpawns(team.getDefaultPossibleSpawns());
        source.sendSuccess(() -> SkyComponents.SUCCESS_RESET_SPAWNS, true);
        return 1;
    }
}
