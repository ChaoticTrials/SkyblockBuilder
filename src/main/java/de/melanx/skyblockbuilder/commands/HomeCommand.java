package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.CompatHelper;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class HomeCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Teleports the player back home
        return Commands.literal("home")
                .executes(context -> home(context.getSource()));
    }

    private static int home(CommandSource source) throws CommandSyntaxException {
        if (!CompatHelper.ALLOW_TEAM_MANAGEMENT) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.compat.disabled_management").mergeStyle(TextFormatting.RED), true);
            return 0;
        }

        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.error.user_has_no_team").mergeStyle(TextFormatting.RED), true);
            return 0;
        }
        
        switch (SkyblockHooks.onHome(player, team)) {
            case DENY:
                source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.denied.teleport_home").mergeStyle(TextFormatting.RED), true);
                return 0;
            case DEFAULT:
                if (!ConfigHandler.homeEnabled.get() && !source.hasPermissionLevel(2)) {
                    source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.disabled.teleport_home").mergeStyle(TextFormatting.RED), true);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.success.teleport_home").mergeStyle(TextFormatting.GOLD), true);
        WorldUtil.teleportToIsland(player, team);
        return 1;
    }
}
