package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class SpawnCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Teleports the player to spawn
        return Commands.literal("spawn").requires(source -> LibXConfigHandler.Utility.Teleports.spawn || source.hasPermissionLevel(2))
                .executes(context -> spawn(context.getSource()));
    }

    private static int spawn(CommandSource source) throws CommandSyntaxException {
        WorldUtil.checkSkyblock(source);
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getSpawn();

        source.sendFeedback(new TranslationTextComponent("skyblockbuilder.command.success.teleport_to_spawn"), false);
        WorldUtil.teleportToIsland(player, team);
        return 1;
    }
}
