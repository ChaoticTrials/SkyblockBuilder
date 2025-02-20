package de.melanx.skyblockbuilder.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.permissions.PermissionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class CommandUtil {

    public static boolean mayNotTeleport(CommandSourceStack source, SkyblockSavedData data, ServerPlayer player) {
        if (!PermissionManager.INSTANCE.mayBypassLimitation(player) && !PermissionsConfig.Teleports.teleportationDimensions.test(player.level().dimension().location())) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.teleportation_not_allowed_dimension"));
            return true;
        }

        if (!PermissionManager.INSTANCE.hasPermission(player, PermissionManager.Permission.TELEPORT_ACROSS_DIMENSIONS) && player.level() != data.getLevel()) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.teleport_across_dimensions"));
            return true;
        }

        if (PermissionsConfig.Teleports.disallowTeleportationDuringFalling && player.fallDistance > 1) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.prevent_while_falling"));
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
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.user_has_no_team"));
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
            source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.error.team_not_exist").withStyle(ChatFormatting.RED), false);
            return null;
        }

        return new ValidationResult(player, data, team);
    }

    public record ValidationResult(ServerPlayer player, SkyblockSavedData data, Team team) {}
}
