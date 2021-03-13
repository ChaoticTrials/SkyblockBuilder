package de.melanx.skyblockbuilder.commands.helper;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.world.IslandPos;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class SpawnsCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Highlights all spawns for a few seconds
        return Commands.literal("spawns")
                .executes(context -> showSpawns(context.getSource()));
    }

    private static int showSpawns(CommandSource source) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        for (Team team : data.getTeams()) {
            IslandPos spawn = team.getIsland();
            for (BlockPos pos : SkyblockSavedData.initialPossibleSpawns(spawn.getCenter())) {
                if (source.getEntity() instanceof ServerPlayerEntity) {
                    world.spawnParticle(source.asPlayer(), ParticleTypes.HAPPY_VILLAGER, true, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 10);
                } else {
                    world.spawnParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 10);
                }
            }
        }

        return 1;
    }
}
