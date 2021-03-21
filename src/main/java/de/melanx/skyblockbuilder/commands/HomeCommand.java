package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class HomeCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Teleports the player back home
        return Commands.literal("home")
                .executes(context -> home(context.getSource()));
    }

    private static int home(CommandSource source) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeamFromPlayer(player);

        if (team == null) {
            source.sendFeedback(new StringTextComponent("You're currently in no team!").mergeStyle(TextFormatting.RED), false);
            return 0;
        }
        
        switch (SkyblockHooks.onHome(player, team)) {
            case DENY:
                source.sendFeedback(new StringTextComponent("You may not teleport home now.").mergeStyle(TextFormatting.RED), false);
                return 0;
            case DEFAULT:
                if (!ConfigHandler.homeEnabled.get() && !source.hasPermissionLevel(2)) {
                    source.sendFeedback(new StringTextComponent("You are not allowed to teleport to your home point..").mergeStyle(TextFormatting.RED), false);
                    return 0;
                }
                break;
            case ALLOW:
                break;
        }

        source.sendFeedback(new StringTextComponent("Home sweet home"), false);
        WorldUtil.teleportToIsland(player, team);
        return 1;
    }
}
