package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class HomeCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Teleports the player back home
        return Commands.literal("home")
                .executes(context -> home(context.getSource()));
    }

    private static int home(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        ServerPlayer player = source.getPlayerOrException();
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        if (!player.hasPermissions(2) && !data.getOrCreateMetaInfo(player).canTeleportHome(level.getGameTime())) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.cooldown",
                    RandomUtility.formattedCooldown(PermissionsConfig.Teleports.homeCooldown - (level.getGameTime() - data.getOrCreateMetaInfo(player).getLastHomeTeleport()))));
            return 0;
        }

        if (!player.hasPermissions(2) && !PermissionsConfig.Teleports.teleportationDimensions.test(player.level().dimension().location())) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.teleportation_not_allowed_dimension"));
            return 0;
        }

        if (!player.hasPermissions(2) && !PermissionsConfig.Teleports.crossDimensionTeleportation && player.level() != data.getLevel()) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.teleport_across_dimensions"));
            return 0;
        }

        if (!player.hasPermissions(2) && PermissionsConfig.Teleports.preventWhileFalling && player.fallDistance > 1) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.prevent_while_falling"));
            return 0;
        }

        switch (SkyblockHooks.onHome(player, team)) {
            case DENY:
                source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.denied.teleport_home").withStyle(ChatFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (!PermissionsConfig.Teleports.home && !source.hasPermission(2)) {
                    source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.disabled.teleport_home").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        data.getOrCreateMetaInfo(player).setLastHomeTeleport(level.getGameTime());
        source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.teleport_home").withStyle(ChatFormatting.GOLD), true);
        WorldUtil.teleportToIsland(player, team);
        return 1;
    }
}
