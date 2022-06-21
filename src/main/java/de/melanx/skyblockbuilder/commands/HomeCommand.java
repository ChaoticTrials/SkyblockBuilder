package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.ConfigHandler;
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
            source.sendSuccess(Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        if (!player.hasPermissions(2) && !data.getMetaInfo(player).canTeleportHome(level.getGameTime())) {
            source.sendFailure(Component.translatable("skyblockbuilder.command.error.cooldown",
                    RandomUtility.formattedCooldown(ConfigHandler.Utility.Teleports.homeCooldown - (level.getGameTime() - data.addMetaInfo(player).getLastHomeTeleport()))));
            return 0;
        }

        switch (SkyblockHooks.onHome(player, team)) {
            case DENY:
                source.sendSuccess(Component.translatable("skyblockbuilder.command.denied.teleport_home").withStyle(ChatFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (!ConfigHandler.Utility.Teleports.home && !source.hasPermission(2)) {
                    source.sendSuccess(Component.translatable("skyblockbuilder.command.disabled.teleport_home").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        data.getMetaInfo(player).setLastHomeTeleport(level.getGameTime());
        source.sendSuccess(Component.translatable("skyblockbuilder.command.success.teleport_home").withStyle(ChatFormatting.GOLD), true);
        WorldUtil.teleportToIsland(player, team);
        return 1;
    }
}
