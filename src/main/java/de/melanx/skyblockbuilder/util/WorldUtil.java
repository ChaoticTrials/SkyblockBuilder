package de.melanx.skyblockbuilder.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.world.dimensions.end.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.nether.SkyblockNetherChunkGenerator;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldChunkGenerator;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WorldUtil {

    public static final ResourceLocation SINGLE_BIOME = LibXConfigHandler.World.SingleBiome.biome;

    public static void teleportToIsland(ServerPlayerEntity player, Team team) {
        MinecraftServer server = player.getServer();
        //noinspection ConstantConditions
        ServerWorld world = getConfiguredWorld(server);

        BlockPos spawn = validPosition(world, team);
        player.teleport(world, spawn.getX() + 0.5, spawn.getY() + 0.5, spawn.getZ() + 0.5, LibXConfigHandler.Spawn.direction.getYaw(), 0);
        player.func_242111_a(world.getDimensionKey(), spawn, 0, true, false);
    }

    public static boolean isSkyblock(World world) {
        if (!(world instanceof ServerWorld)) return false;

        MinecraftServer server = ((ServerWorld) world).getServer();

        if (!LibXConfigHandler.Dimensions.Overworld.Default) {
            return server.func_241755_D_().getChunkProvider().getChunkGenerator() instanceof SkyblockOverworldChunkGenerator;
        }

        if (!LibXConfigHandler.Dimensions.Nether.Default) {
            ServerWorld nether = server.getWorld(World.THE_NETHER);
            return nether != null && nether.getChunkProvider().getChunkGenerator() instanceof SkyblockNetherChunkGenerator;
        }

        if (!LibXConfigHandler.Dimensions.End.Default) {
            ServerWorld end = server.getWorld(World.THE_END);
            return end != null && end.getChunkProvider().getChunkGenerator() instanceof SkyblockEndChunkGenerator;
        }

        return false;
    }

    public static void checkSkyblock(CommandSource source) throws CommandSyntaxException {
        if (!isSkyblock(source.getServer().func_241755_D_())) {
            throw new SimpleCommandExceptionType(new TranslationTextComponent("skyblockbuilder.error.no_skyblock")).create();
        }
    }

    public static ServerWorld getConfiguredWorld(MinecraftServer server) {
        ResourceLocation location = LibXConfigHandler.Spawn.dimension;
        RegistryKey<World> worldKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, location);
        ServerWorld configWorld = server.getWorld(worldKey);

        if (configWorld == null) {
            SkyblockBuilder.getLogger().warn("Configured dimension for spawn does not exist: " + location);
        }

        return configWorld != null ? configWorld : server.func_241755_D_();
    }

    private static BlockPos validPosition(ServerWorld world, Team team) {

        List<BlockPos> spawns = new ArrayList<>(team.getPossibleSpawns());
        Random random = new Random();
        while (!spawns.isEmpty()) {
            BlockPos pos = spawns.get(random.nextInt(spawns.size()));
            if (!world.getBlockState(pos.down()).getCollisionShape(world, pos.down()).isEmpty()) {
                return pos;
            }

            spawns.remove(pos);
        }

        BlockPos pos = team.getPossibleSpawns().stream().findAny().orElse(BlockPos.ZERO);
        BlockPos.Mutable mpos = new BlockPos.Mutable(pos.getX(), world.getHeight(), pos.getZ());
        Spiral spiral = new Spiral();
        while (world.getBlockState(mpos.down()).getCollisionShape(world, mpos.down()).isEmpty()) {
            if (mpos.getY() <= 0) {
                if (spiral.getX() > LibXConfigHandler.Spawn.radius || spiral.getY() > LibXConfigHandler.Spawn.radius) {
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
                SkyblockBuilder.getLogger().error("Error while parsing surface settings string => {}", numberformatexception.getMessage());
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
            SkyblockBuilder.getLogger().error("Error while parsing surface settings string => {}", exception.getMessage());
            return null;
        }

        if (block == null) {
            SkyblockBuilder.getLogger().error("Error while parsing surface settings string => Unknown block, {}", blockName);
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

    public enum SingleBiomeDimension {
        DEFAULT(null),
        OVERWORLD(World.OVERWORLD.getLocation()),
        THE_NETHER(World.THE_NETHER.getLocation()),
        THE_END(World.THE_END.getLocation());

        private final ResourceLocation singleBiomeDimension;

        SingleBiomeDimension(ResourceLocation dimension) {
            if (dimension == null) {
                this.singleBiomeDimension = LibXConfigHandler.Spawn.dimension;
            } else {
                this.singleBiomeDimension = dimension;
            }
        }

        public ResourceLocation getDimension() {
            return this.singleBiomeDimension;
        }
    }
}
