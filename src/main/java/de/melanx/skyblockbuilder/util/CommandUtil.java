package de.melanx.skyblockbuilder.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.compat.infiniverse.InfiniverseCompat;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class CommandUtil {

    public static boolean mayNotTeleport(CommandSourceStack source, SkyblockSavedData data, ServerPlayer player) {
        if (!PermissionManager.INSTANCE.mayBypassLimitation(player) && !PermissionsConfig.Teleports.teleportationDimensions.test(player.level().dimension().identifier())) {
            source.sendFailure(SkyComponents.ERROR_TELEPORTATION_NOT_ALLOWED_DIMENSION);
            return true;
        }

        if (!InfiniverseCompat.useInfiniverse() && !PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_ACROSS_DIMENSIONS) && player.level() != data.getLevel()) {
            source.sendFailure(SkyComponents.ERROR_TELEPORT_ACROSS_DIMENSIONS);
            return true;
        }

        if (PermissionsConfig.Teleports.disallowTeleportationDuringFalling && player.fallDistance > 1) {
            source.sendFailure(SkyComponents.ERROR_PREVENT_WHILE_FALLING);
            return true;
        }

        return false;
    }

    @Nullable
    public static ValidationResult validatePlayerTeam(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        ServerPlayer player = source.getPlayerOrException();

        Team team = data.getTeamFromPlayer(player);
        if (team == null) {
            source.sendFailure(SkyComponents.ERROR_USER_HAS_NO_TEAM);
            return null;
        }

        return new ValidationResult(player, data, team);
    }

    @Nullable
    public static ValidationResult validateTeamExistence(CommandSourceStack source, String name) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        ServerPlayer player = source.getPlayerOrException();
        Team team = data.getTeam(name);

        if (team == null) {
            source.sendFailure(SkyComponents.ERROR_TEAM_NOT_EXIST);
            return null;
        }

        return new ValidationResult(player, data, team);
    }

    public record ValidationResult(ServerPlayer player, SkyblockSavedData data, Team team) {}
}
