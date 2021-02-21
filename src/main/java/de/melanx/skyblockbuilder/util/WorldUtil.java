package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.world.IslandPos;
import de.melanx.skyblockbuilder.world.VoidChunkGenerator;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class WorldUtil {
    public static void teleportToIsland(ServerPlayerEntity player, IslandPos island) {
        ServerWorld world = player.getServerWorld();

        Set<BlockPos> possibleSpawns = SkyblockSavedData.get(world).getPossibleSpawns(island);
        BlockPos spawn = new ArrayList<>(possibleSpawns).get(new Random().nextInt(possibleSpawns.size()));
        player.rotationYaw = 0;
        player.rotationPitch = 0;
        player.setPositionAndUpdate(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        player.func_242111_a(player.world.getDimensionKey(), spawn, 0, true, false);
    }

    public static boolean isSkyblock(World world) {
        return world.getChunkProvider() instanceof ServerChunkProvider &&
                ((ServerChunkProvider) world.getChunkProvider()).getChunkGenerator() instanceof VoidChunkGenerator;
    }
}
