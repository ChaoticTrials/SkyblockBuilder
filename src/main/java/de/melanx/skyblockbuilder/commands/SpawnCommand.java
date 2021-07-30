package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SpawnCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        // Teleports the player to spawn
        return Commands.literal("spawn").requires(source -> LibXConfigHandler.Utility.Teleports.spawn || source.hasPermission(2))
                .executes(context -> spawn(context.getSource()));
    }

    private static int spawn(CommandSourceStack source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerLevel level = source.getLevel();
        SkyblockSavedData data = SkyblockSavedData.get(level);

        ServerPlayer player = source.getPlayerOrException();
        Team team = data.getSpawn();

        source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.success.teleport_to_spawn"), false);
        WorldUtil.teleportToIsland(player, team);
        return 1;
    }
}
