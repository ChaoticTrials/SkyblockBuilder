package de.melanx.skyblockbuilder.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.melanx.skyblockbuilder.ModBlockTags;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.config.SpawnSettings;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WorldUtil {

    public static void teleportToIsland(ServerPlayer player, Team team) {
        MinecraftServer server = player.getServer();
        //noinspection ConstantConditions
        ServerLevel level = getConfiguredLevel(server);

        BlockPos spawn = validPosition(level, team);
        player.teleportTo(level, spawn.getX() + 0.5, spawn.getY() + 0.2, spawn.getZ() + 0.5, team.getDirection().getYRot(), 0);
        player.setRespawnPosition(level.dimension(), spawn, 0, true, false);
    }

    public static boolean isSkyblock(Level level) {
        if (!(level instanceof ServerLevel)) return false;

        MinecraftServer server = ((ServerLevel) level).getServer();

        if (!ConfigHandler.Dimensions.Overworld.Default) {
            return server.overworld().getChunkSource().getGenerator() instanceof SkyblockNoiseBasedChunkGenerator;
        }

        if (!ConfigHandler.Dimensions.Nether.Default) {
            ServerLevel nether = server.getLevel(Level.NETHER);
            return nether != null && nether.getChunkSource().getGenerator() instanceof SkyblockNoiseBasedChunkGenerator;
        }

        if (!ConfigHandler.Dimensions.End.Default) {
            ServerLevel end = server.getLevel(Level.END);
            return end != null && end.getChunkSource().getGenerator() instanceof SkyblockEndChunkGenerator;
        }

        return false;
    }

    public static void checkSkyblock(CommandSourceStack source) throws CommandSyntaxException {
        if (!isSkyblock(source.getServer().overworld())) {
            throw new SimpleCommandExceptionType(Component.translatable("skyblockbuilder.error.no_skyblock")).create();
        }
    }

    public static ServerLevel getConfiguredLevel(MinecraftServer server) {
        ResourceLocation location = ConfigHandler.Spawn.dimension.location();
        ResourceKey<Level> worldKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, location);
        ServerLevel configLevel = server.getLevel(worldKey);

        if (configLevel == null) {
            SkyblockBuilder.getLogger().warn("Configured dimension for spawn does not exist: " + location);
        }

        return configLevel != null ? configLevel : server.overworld();
    }

    private static BlockPos validPosition(ServerLevel level, Team team) {
        List<BlockPos> spawns = new ArrayList<>(team.getPossibleSpawns());
        Random random = new Random();
        while (!spawns.isEmpty()) {
            BlockPos pos = spawns.get(random.nextInt(spawns.size()));
            if (isValidSpawn(level, pos)) {
                return pos;
            }

            spawns.remove(pos);
        }

        BlockPos pos = team.getPossibleSpawns().stream().findAny().orElse(BlockPos.ZERO);

        return PositionHelper.findPos(pos, blockPos -> isValidSpawn(level, blockPos), ConfigHandler.Spawn.radius);
    }

    public static boolean isValidSpawn(Level level, BlockPos pos) {
        return WorldUtil.isValidSpawn(level, pos, level.getMinBuildHeight(), level.getMaxBuildHeight());
    }

    public static boolean isValidSpawn(Level level, BlockPos pos, int bottom, int top) {
        return pos.getY() >= bottom
                && pos.getY() <= top
                && (level.getBlockState(pos.below()).canOcclude() || level.getBlockState(pos.below()).is(ModBlockTags.ADDITIONAL_VALID_SPAWN))
                && !level.getBlockState(pos).canOcclude()
                && !level.getBlockState(pos.above()).canOcclude();
    }

    public static int calcSpawnHeight(Level level, int x, int z) {
        int top = ConfigHandler.Spawn.Height.range.top();
        int bottom = ConfigHandler.Spawn.Height.range.bottom();

        int height;
        switch (ConfigHandler.Spawn.Height.spawnType) {
            case RANGE_TOP, RANGE_BOTTOM -> {
                BlockPos.MutableBlockPos spawn = new BlockPos.MutableBlockPos(x, top, z);
                while (!WorldUtil.isValidSpawn(level, spawn, bottom, top)) {
                    if (spawn.getY() <= level.getMinBuildHeight()) {
                        if (ConfigHandler.Spawn.Height.spawnType == SpawnSettings.Type.RANGE_TOP) {
                            spawn.setY(top);
                        } else {
                            spawn.setY(bottom);
                        }
                        break;
                    }

                    spawn.move(Direction.DOWN, 1);
                }
                height = spawn.getY() + ConfigHandler.Spawn.Height.offset;
            }
            // SpawnSettings.Type.SET
            default -> height = bottom;
        }

        return Math.max(level.getMinBuildHeight() + 1, height);
    }

    // [Vanilla copy] Get flat world info on servers
    public static List<FlatLayerInfo> layersInfoFromString(String settings) {
        if (settings == null) {
            return Lists.newArrayList();
        }

        List<FlatLayerInfo> list = Lists.newArrayList();
        String[] astring = settings.split(",");
        int i = 0;

        for (String s : astring) {
            FlatLayerInfo flatlayerinfo = getLayerInfo(s, i);
            if (flatlayerinfo == null) {
                return Collections.emptyList();
            }

            list.add(flatlayerinfo);
            i += flatlayerinfo.getHeight();
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
                SkyblockBuilder.getLogger().error("Error while parsing surface settings string => {}", numberformatexception.getMessage());
                return null;
            }
        } else {
            i = 1;
        }

        int maxLayers = Math.min(currentLayers + i, 384);
        int height = maxLayers - currentLayers;
        String blockName = info[info.length - 1];

        Block block;
        try {
            block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
        } catch (Exception exception) {
            SkyblockBuilder.getLogger().error("Error while parsing surface settings string => {}", exception.getMessage());
            return null;
        }

        if (block == null) {
            SkyblockBuilder.getLogger().error("Error while parsing surface settings string => Unknown block, {}", blockName);
            return null;
        } else {
            return new FlatLayerInfo(height, block);
        }
    }

    public static int calculateHeightFromLayers(List<FlatLayerInfo> layerInfos) {
        int i = 0;
        for (FlatLayerInfo info : layerInfos) {
            i += info.getHeight();
        }

        return i;
    }

    public enum Directions {
        NORTH(180),
        EAST(270),
        SOUTH(0),
        WEST(90);

        private final int yRot;

        Directions(int yaw) {
            this.yRot = yaw;
        }

        public int getYRot() {
            return this.yRot;
        }
    }
}
