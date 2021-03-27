package de.melanx.skyblockbuilder.commands.helper;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Set;

public class SpawnsCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        // Highlights all spawns for a few seconds
        return Commands.literal("spawns")
                .executes(context -> showSpawns(context.getSource(), false))
                // use debug for setting up a new spawn points as pack author
                .then(Commands.argument("debug", BoolArgumentType.bool()).requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> showSpawns(context.getSource(), BoolArgumentType.getBool(context, "debug"))));
    }

    private static int showSpawns(CommandSource source, boolean debug) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        for (Team team : data.getTeams()) {
            IslandPos spawn = team.getIsland();
            Set<BlockPos> posSet = debug ? SkyblockSavedData.initialPossibleSpawns(spawn.getCenter()) : team.getPossibleSpawns();
            for (BlockPos pos : posSet) {
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
