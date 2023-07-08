package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.common.InventoryConfig;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class LeaveCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Let the player leave a team
        return Commands.literal("leave")
                .executes(context -> leaveTeam(context.getSource()));
    }

    private static int leaveTeam(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        ServerPlayer player = source.getPlayerOrException();

        if (!data.hasPlayerTeam(player)) {
            source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.error.user_has_no_team").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        switch (SkyblockHooks.onLeave(player, data.getTeamFromPlayer(player))) {
            case DENY:
                source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.denied.leave_team").withStyle(ChatFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (!PermissionsConfig.selfManage && !source.hasPermission(2)) {
                    source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.disabled.manage_teams").withStyle(ChatFormatting.RED), false);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        if (InventoryConfig.dropItems) {
            RandomUtility.dropInventories(player);
        }
        data.removePlayerFromTeam(player);
        source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.left_team").withStyle(ChatFormatting.GOLD), true);
        WorldUtil.teleportToIsland(player, data.getSpawn());
        return 1;
    }
}
