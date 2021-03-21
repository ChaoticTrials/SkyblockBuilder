package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.world.IslandPos;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldChunkGenerator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WorldUtil {
    
    public static void teleportToIsland(ServerPlayerEntity player, Team team) {
        //noinspection ConstantConditions
        ServerWorld world = player.getServer().func_241755_D_();
        IslandPos island = team.getIsland();

        Set<BlockPos> possibleSpawns = SkyblockSavedData.get(world).getPossibleSpawns(island);
        BlockPos spawn = validPosition(world, team);
        player.teleport(world, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, ConfigHandler.direction.get().getYaw(), 0);
        player.func_242111_a(world.getDimensionKey(), spawn, 0, true, false);
    }

    public static boolean isSkyblock(World world) {
        return world.getChunkProvider() instanceof ServerChunkProvider &&
                ((ServerChunkProvider) world.getChunkProvider()).getChunkGenerator() instanceof SkyblockOverworldChunkGenerator;
    }

    private static BlockPos validPosition(ServerWorld world, Team team) {
        
        List<BlockPos> spawns = new ArrayList<>(team.getPossibleSpawns());
        Random random = new Random();
        while (!spawns.isEmpty()) {
            BlockPos pos = spawns.get(random.nextInt(spawns.size()));
            //noinspection deprecation
            if (!world.getBlockState(pos.down()).isAir(world, pos.down())) {
                return pos;
            }

            spawns.remove(pos);
        }

        BlockPos pos = team.getPossibleSpawns().stream().findAny().orElse(BlockPos.ZERO);
        BlockPos.Mutable mpos = new BlockPos.Mutable(pos.getX(), world.getHeight(), pos.getZ());
        Spiral spiral = new Spiral();
        //noinspection deprecation
        while (world.getBlockState(mpos.down()).isAir(world, mpos.down())) {
            if (mpos.getY() <= 0) {
                if (spiral.getX() > ConfigHandler.spawnRadius.get() || spiral.getY() > ConfigHandler.spawnRadius.get()) {
                    return pos;
                }
                spiral.next();

                mpos.setX(pos.getX() + spiral.getX());
                mpos.setY(world.getHeight());
                mpos.setZ(pos.getZ() + spiral.getY());
            }

            mpos.move(Direction.DOWN);
        }

        return mpos;
    }

    public enum Directions {
        NORTH(180),
        EAST(270),
        SOUTH(0),
        WEST(90);

        private final int yaw;

        Directions(int yaw) {
            this.yaw = yaw;
        }

        public int getYaw() {
            return this.yaw;
        }
    }
}
