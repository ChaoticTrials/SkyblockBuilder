package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class SpawnCommand {
    
    public static ArgumentBuilder<CommandSource, ?> register() {
        // Teleports the player to spawn
        return Commands.literal("spawn").requires(source -> ConfigHandler.spawnTeleport.get() || source.hasPermissionLevel(2))
                .executes(context -> spawn(context.getSource()));
    }

    private static int spawn(CommandSource source) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        ServerPlayerEntity player = source.asPlayer();
        Team team = data.getSpawn();

        source.sendFeedback(new StringTextComponent("Successfully teleported to spawn."), false);
        WorldUtil.teleportToIsland(player, team);
        return 1;
    }
}
