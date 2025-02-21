package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyMeta;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import de.melanx.skyblockbuilder.util.CommandUtil;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class VisitCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Let the player visit another team
        return Commands.literal("visit")
                .then(Commands.argument("team", StringArgumentType.string()).suggests(Suggestions.VISIT_TEAMS)
                        .executes(context -> visit(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int visit(CommandSourceStack source, String name) throws CommandSyntaxException {
        CommandUtil.ValidationResult validationResult = CommandUtil.validateTeamExistence(source, name);
        if (validationResult == null) {
            return 0;
        }

        ServerPlayer player = validationResult.player();
        Level level = player.level();
        SkyblockSavedData data = validationResult.data();
        Team team = validationResult.team();

        if (!PermissionManager.INSTANCE.mayBypassLimitation(player) && !data.getOrCreateMetaInfo(player).canTeleport(SkyMeta.TeleportType.VISIT, level.getGameTime())) {
            source.sendFailure(SkyComponents.ERROR_COOLDOWN.apply(
                    RandomUtility.formattedCooldown(PermissionsConfig.Teleports.Cooldowns.visitCooldown - (level.getGameTime() - data.getOrCreateMetaInfo(player).getLastTeleport(SkyMeta.TeleportType.VISIT)))
            ));
            return 0;
        }

        if (!PermissionManager.INSTANCE.mayBypassLimitation(player) && !PermissionsConfig.Teleports.teleportationDimensions.test(level.dimension().location())) {
            source.sendFailure(SkyComponents.ERROR_TELEPORTATION_NOT_ALLOWED_DIMENSION);
            return 0;
        }

        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_ACROSS_DIMENSIONS) && level != data.getLevel()) {
            source.sendFailure(SkyComponents.ERROR_TELEPORT_ACROSS_DIMENSIONS);
            return 0;
        }

        switch (SkyblockHooks.onVisit(player, team)) {
            case DENY:
                source.sendFailure(SkyComponents.DISABLED_VISIT_TEAM);
                return 0;
            case DEFAULT:
                if (team.hasPlayer(player)) {
                    source.sendFailure(SkyComponents.ERROR_VISIT_OWN_TEAM);
                    return 0;
                }
                if (!PermissionManager.INSTANCE.mayExecuteOpCommand(player)) {
                    if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_TO_VISITING_ISLAND)) {
                        source.sendFailure(SkyComponents.DISABLED_TEAM_VISIT);
                        return 0;
                    }

                    if (!team.allowsVisits()) {
                        source.sendFailure(SkyComponents.DISABLED_VISIT_TEAM);
                        return 0;
                    }
                }
                break;
            case ALLOW:
                break;
        }

        WorldUtil.teleportToIsland(player, team);
        data.getOrCreateMetaInfo(player).setLastTeleport(SkyMeta.TeleportType.VISIT, level.getGameTime());
        source.sendSuccess(() -> SkyComponents.SUCCESS_VISIT_TEAM.apply(name), true);
        return 1;
    }
}
