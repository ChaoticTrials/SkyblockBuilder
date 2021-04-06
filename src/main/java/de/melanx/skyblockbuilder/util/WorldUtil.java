package de.melanx.skyblockbuilder.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.world.IslandPos;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldChunkGenerator;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

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
    
    public static void checkSkyblock(CommandSource source) throws CommandSyntaxException {
        if (!isSkyblock(source.getServer().func_241755_D_())) {
            throw new SimpleCommandExceptionType(new TranslationTextComponent("skyblockbuilder.error.no_skyblock")).create();
        }
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

    // [Vanilla copy] Get flat world info on servers
    public static List<FlatLayerInfo> layersInfoFromString(String settings) {
        List<FlatLayerInfo> list = Lists.newArrayList();
        String[] astring = settings.split(",");
        int i = 0;

        for (String s : astring) {
            FlatLayerInfo flatlayerinfo = getLayerInfo(s, i);
            if (flatlayerinfo == null) {
                return Collections.emptyList();
            }

            list.add(flatlayerinfo);
            i += flatlayerinfo.getLayerCount();
        }

        return list;
    }

    // [Vanilla copy]
    @Nullable
    private static FlatLayerInfo getLayerInfo(String setting, int currentLayers) {
        String[] info = setting.split("\\*", 2);
        int i;
        if (info.length == 2) {
            try {
                i = Math.max(Integer.parseInt(info[0]), 0);
            } catch (NumberFormatException numberformatexception) {
                SkyblockBuilder.LOGGER.error("Error while parsing surface settings string => {}", numberformatexception.getMessage());
                return null;
            }
        } else {
            i = 1;
        }

        int maxLayers = Math.min(currentLayers + i, 256);
        int height = maxLayers - currentLayers;
        String blockName = info[info.length - 1];

        Block block;
        try {
            block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
        } catch (Exception exception) {
            SkyblockBuilder.LOGGER.error("Error while parsing surface settings string => {}", exception.getMessage());
            return null;
        }

        if (block == null) {
            SkyblockBuilder.LOGGER.error("Error while parsing surface settings string => Unknown block, {}", blockName);
            return null;
        } else {
            FlatLayerInfo layerInfo = new FlatLayerInfo(height, block);
            layerInfo.setMinY(currentLayers);
            return layerInfo;
        }
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
