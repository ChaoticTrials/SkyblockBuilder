package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.SpawnSettings;
import de.melanx.skyblockbuilder.config.common.*;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.registration.ModBlockTags;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockEndChunkGenerator;
import de.melanx.skyblockbuilder.world.chunkgenerators.SkyblockNoiseBasedChunkGenerator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldUtil {

    public static void teleportToIsland(ServerPlayer player, Team team) {
        MinecraftServer server = player.getServer();

        if (WorldConfig.leaveToOverworld && team.isSpawn()) {
            Team playersTeam = SkyblockSavedData.get(player.serverLevel()).getTeamFromPlayer(player);
            if (playersTeam == null || playersTeam.isSpawn()) {
                //noinspection DataFlowIssue
                ServerLevel overworld = server.overworld();
                BlockPos worldSpawn = overworld.getSharedSpawnPos();
                player.teleportTo(overworld, worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ(), 0, 0);
                return;
            }
        }

        //noinspection ConstantConditions
        ServerLevel level = WorldUtil.getConfiguredLevel(server);

        TemplatesConfig.Spawn spawn = validPosition(level, team);
        player.teleportTo(level, spawn.pos().getX() + 0.5, spawn.pos().getY() + 0.2, spawn.pos().getZ() + 0.5, spawn.direction().getYRot(), 0);
        player.setRespawnPosition(level.dimension(), spawn.pos(), spawn.direction().getYRot(), true, false);
        if (PermissionsConfig.Teleports.negateFallDamage) {
            player.fallDistance = 0;
        }
    }

    public static boolean isSkyblock(Level level) {
        if (!(level instanceof ServerLevel)) return false;
        if (PermissionsConfig.forceSkyblockCheck) return true;

        MinecraftServer server = ((ServerLevel) level).getServer();

        if (DimensionsConfig.Overworld.isCustom) {
            return server.overworld().getChunkSource().getGenerator() instanceof SkyblockNoiseBasedChunkGenerator;
        }

        if (DimensionsConfig.Nether.isCustom) {
            ServerLevel nether = server.getLevel(Level.NETHER);
            return nether != null && nether.getChunkSource().getGenerator() instanceof SkyblockNoiseBasedChunkGenerator;
        }

        if (DimensionsConfig.End.isCustom) {
            ServerLevel end = server.getLevel(Level.END);
            return end != null && end.getChunkSource().getGenerator() instanceof SkyblockEndChunkGenerator;
        }

        return false;
    }

    public static void checkSkyblock(CommandSourceStack source) throws CommandSyntaxException {
        if (!isSkyblock(source.getServer().overworld())) {
            throw new SimpleCommandExceptionType(SkyComponents.NO_SKYBLOCK).create();
        }
    }

    public static ServerLevel getConfiguredLevel(MinecraftServer server) {
        ResourceLocation location = SpawnConfig.spawnDimension.location();
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, location);
        ServerLevel configLevel = server.getLevel(worldKey);

        if (configLevel == null) {
            SkyblockBuilder.getLogger().warn("Configured dimension for spawn does not exist: {}", location);
        }

        return configLevel != null ? configLevel : server.overworld();
    }

    private static TemplatesConfig.Spawn validPosition(ServerLevel level, Team team) {
        List<TemplatesConfig.Spawn> spawns = new ArrayList<>(team.getPossibleSpawns());
        Random random = new Random();
        while (!spawns.isEmpty()) {
            TemplatesConfig.Spawn spawn = spawns.get(random.nextInt(spawns.size()));
            if (isValidSpawn(level, spawn.pos())) {
                return spawn;
            }

            spawns.remove(spawn);
        }

        SkyblockBuilder.getLogger().info("No valid spawn position found, searching...");
        TemplatesConfig.Spawn spawn = team.getPossibleSpawns().stream().findAny().orElse(new TemplatesConfig.Spawn(team.getIsland().getCenter(), SpawnDirection.SOUTH));

        return new TemplatesConfig.Spawn(PositionHelper.findPos(spawn.pos(), blockPos -> isValidSpawn(level, blockPos), SpawnConfig.radiusToFindValidSpawn), spawn.direction());
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
        int top = SpawnConfig.Height.range.top();
        int bottom = SpawnConfig.Height.range.bottom();

        int height = switch (SpawnConfig.Height.heightCalculationType) {
            case RANGE_TOP, RANGE_BOTTOM -> {
                BlockPos.MutableBlockPos spawn = new BlockPos.MutableBlockPos(x, top, z);
                while (!WorldUtil.isValidSpawn(level, spawn, bottom, top)) {
                    if (spawn.getY() <= level.getMinBuildHeight()) {
                        if (SpawnConfig.Height.heightCalculationType == SpawnSettings.Type.RANGE_TOP) {
                            spawn.setY(top);
                        } else {
                            spawn.setY(bottom);
                        }
                        break;
                    }

                    spawn.move(Direction.DOWN, 1);
                }
                yield spawn.getY() + SpawnConfig.Height.offset;
            }
            case SET -> bottom;
        };

        return Math.max(level.getMinBuildHeight() + 1, height);
    }

    public static CompoundTag blockPosToTag(BlockPos pos) {
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("posX", pos.getX());
        posTag.putInt("posY", pos.getY());
        posTag.putInt("posZ", pos.getZ());

        return posTag;
    }

    public static BlockPos blockPosFromTag(CompoundTag posTag) {
        return new BlockPos(
                posTag.getInt("posX"),
                posTag.getInt("posY"),
                posTag.getInt("posZ")
        );
    }

    public static BlockPos blockPosFromJsonArray(JsonArray json) {
        if (json.size() != 3) throw new IllegalStateException("Invalid BlockPos: " + json);
        return new BlockPos(
                json.get(0).getAsInt(),
                json.get(1).getAsInt(),
                json.get(2).getAsInt()
        );
    }

    public static JsonArray blockPosToJsonArray(BlockPos pos) {
        JsonArray array = new JsonArray();
        array.add(pos.getX());
        array.add(pos.getY());
        array.add(pos.getZ());
        return array;
    }

    public enum SpawnDirection {
        NORTH(180),
        EAST(270),
        SOUTH(0),
        WEST(90);

        private final int yRot;

        SpawnDirection(int yaw) {
            this.yRot = yaw;
        }

        public static SpawnDirection fromDirection(Direction direction) {
            return switch (direction) {
                case NORTH -> NORTH;
                case EAST -> EAST;
                case WEST -> WEST;
                default -> SOUTH;
            };
        }

        public int getYRot() {
            return this.yRot;
        }
    }
}
