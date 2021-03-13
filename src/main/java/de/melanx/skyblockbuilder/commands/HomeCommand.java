package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
        return Commands.literal("home")
                .executes(context -> home(context.getSource()));
    }

    private static int home(CommandSource source) throws CommandSyntaxException {
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            return 0;
        }
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);
        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getTeamFromPlayer(player);
        if (team == null) {
            source.sendFeedback(new StringTextComponent("You currently in no team!").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        WorldUtil.teleportToIsland(player, data.getTeamFromPlayer(player).getIsland());
        return 1;
    }
}
