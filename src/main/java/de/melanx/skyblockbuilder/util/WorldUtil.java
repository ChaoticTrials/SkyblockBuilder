package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.compat.infiniverse.InfiniverseCompat;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class WorldUtil {

    public static void teleportToIsland(ServerPlayer player, Team team) {
        MinecraftServer server = player.level().getServer();

        if (WorldConfig.leaveToOverworld && team.isSpawn()) {
            Team playersTeam = SkyblockSavedData.get(player.level()).getTeamFromPlayer(player);
            if (playersTeam == null || playersTeam.isSpawn()) {
                ServerLevel overworld = server.findRespawnDimension();
                LevelData.RespawnData respawn = overworld.getRespawnData();
                Vec3 pos = player.adjustSpawnLocation(overworld, respawn.pos()).getBottomCenter();
                player.teleportTo(overworld, pos.x, pos.y, pos.z, Set.of(), respawn.yaw(), respawn.pitch(), false);
                return;
            }
        }

        ServerLevel level;
        if (InfiniverseCompat.useInfiniverse()) {
            ResourceKey<Level> teamLevelKey = team.getTeamLevelKey();
            level = server.getLevel(teamLevelKey);
            if (level == null) {
                SkyblockBuilder.getLogger().error("Team dimension {} is unavailable", teamLevelKey.identifier());
                return;
            }
        } else {
            level = WorldUtil.getConfiguredLevel(server);
        }

        TemplatesConfig.Spawn spawn = WorldUtil.validPosition(level, team);
        player.teleportTo(level, spawn.pos().getX() + 0.5, spawn.pos().getY() + 0.2, spawn.pos().getZ() + 0.5, Set.of(), spawn.direction().getYRot(), 0, false);

        if (player.getRespawnConfig() == null && team.hasPlayer(player)) {
            player.setRespawnPosition(new ServerPlayer.RespawnConfig(LevelData.RespawnData.of(level.dimension(), spawn.pos(), spawn.direction().getYRot(), 0f), true), false);
        }

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

    // Check for spawn level is using Infiniverse
    public static boolean isSpawnDimension(Level level) {
        ResourceKey<Level> dimension = level.dimension();

        return dimension == SpawnConfig.spawnDimension || dimension == Team.SPAWN_LEVEL_KEY;
    }

    // The dimension a team dimension is a copy of, every other dimension is its own original
    public static ResourceKey<Level> resolveOriginalDimension(ResourceKey<Level> dimension) {
        Identifier identifier = dimension.identifier();
        if (!identifier.getNamespace().equals(SkyblockBuilder.getInstance().modid)) {
            return dimension;
        }

        // the spawn island is a copy of the configured spawn dimension, just like the main dimension of a team
        if (dimension == Team.SPAWN_LEVEL_KEY) {
            return SpawnConfig.spawnDimension;
        }

        String path = identifier.getPath();

        if (path.endsWith("_overworld")) {
            return Level.OVERWORLD;
        }

        if (path.endsWith("_nether")) {
            return Level.NETHER;
        }

        if (path.endsWith("_main")) {
            return SpawnConfig.spawnDimension;
        }

        return dimension;
    }

    // Vanilla only allows portals in the overworld and the nether. A team dimension is never one of them, so the
    // dimensions replacing them for a team have to be allowed as well. Dimensions based on a custom spawn dimension
    // are not, as the dimension they are a copy of would not allow portals either.
    public static boolean isTeamPortalDimension(Level level) {
        if (!InfiniverseCompat.useInfiniverse()) {
            return false;
        }

        if (!level.dimension().identifier().getNamespace().equals(SkyblockBuilder.getInstance().modid)) {
            return false;
        }

        ResourceKey<Level> original = WorldUtil.resolveOriginalDimension(level.dimension());

        return original == Level.OVERWORLD || original == Level.NETHER;
    }

    public static boolean isNetherDimension(Level level) {
        return WorldUtil.resolveOriginalDimension(level.dimension()) == Level.NETHER;
    }

    // Portals always lead to the vanilla dimensions. If the player belongs to a team, they have to lead to the
    // dimensions of that team instead. Dimensions a team does not have are left shared with everyone.
    public static ResourceKey<Level> resolvePortalDestination(Entity entity, Level currentLevel, ResourceKey<Level> destination) {
        if (!InfiniverseCompat.useInfiniverse() || (destination != Level.OVERWORLD && destination != Level.NETHER)) {
            return destination;
        }

        ResourceKey<Level> requestedDestination = WorldUtil.isNetherDimension(currentLevel) ? Level.OVERWORLD : Level.NETHER;

        return WorldUtil.resolveTeamDimension(entity, requestedDestination);
    }

    // Use this for every teleport of a player that is not a portal, the position tells apart a world spawn from a real overworld
    public static ResourceKey<Level> resolveTeleportDestination(ServerPlayer player, ResourceKey<Level> destination, Vec3 position) {
        if (InfiniverseCompat.useInfiniverse() && destination == Level.OVERWORLD) {
            LevelData.RespawnData respawn = player.level().getServer().getRespawnData();
            if (respawn.dimension() != destination && respawn.pos().equals(BlockPos.containing(position))) {
                return respawn.dimension();
            }
        }

        return WorldUtil.resolveTeamDimension(player, destination);
    }

    // Use this only when no position is at hand, the two methods above wrap it with the destination they trust
    public static ResourceKey<Level> resolveTeamDimension(Entity entity, ResourceKey<Level> destination) {
        if (!InfiniverseCompat.useInfiniverse() || (destination != Level.OVERWORLD && destination != Level.NETHER)) {
            return destination;
        }

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return destination;
        }

        MinecraftServer server = serverLevel.getServer();
        SkyblockSavedData data = SkyblockSavedData.get(server.overworld());
        Team team = data.getTeamFromPlayer(entity.getUUID());
        if (team == null) {
            Team spawn = data.getSpawn();
            if (!spawn.getPlayers().contains(entity.getUUID())) {
                return destination;
            }

            team = spawn;
        }

        boolean toOverworld = destination == Level.OVERWORLD;
        ResourceKey<Level> teamLevelKey = toOverworld
                ? team.getTeamOverworldLevelKey()
                : team.getTeamNetherLevelKey();

        if (server.getLevel(teamLevelKey) != null) {
            return teamLevelKey;
        }

        // the spawn team has no own overworld, so the island itself is the way back
        if (toOverworld && server.getLevel(team.getTeamLevelKey()) != null) {
            return team.getTeamLevelKey();
        }

        return destination;
    }

    public static void checkSkyblock(CommandSourceStack source) throws CommandSyntaxException {
        if (!isSkyblock(source.getServer().overworld())) {
            throw new SimpleCommandExceptionType(SkyComponents.NO_SKYBLOCK).create();
        }
    }

    public static ServerLevel getConfiguredLevel(MinecraftServer server) {
        Identifier location = SpawnConfig.spawnDimension.identifier();
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
        return WorldUtil.isValidSpawn(level, pos, level.getMinY(), level.getMaxY());
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

        int height = switch(SpawnConfig.Height.heightCalculationType) {
            case RANGE_TOP, RANGE_BOTTOM -> {
                BlockPos.MutableBlockPos spawn = new BlockPos.MutableBlockPos(x, top, z);
                while (!WorldUtil.isValidSpawn(level, spawn, bottom, top)) {
                    if (spawn.getY() <= level.getMinY()) {
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

        return Math.max(level.getMinY() + 1, height);
    }

    public static CompoundTag blockPosToTag(BlockPos pos) {
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("posX", pos.getX());
        posTag.putInt("posY", pos.getY());
        posTag.putInt("posZ", pos.getZ());

        return posTag;
    }

    public static BlockPos blockPosFromTag(Optional<CompoundTag> posTag) {
        return posTag.map(compoundTag -> new BlockPos(
                compoundTag.getIntOr("posX", 0),
                compoundTag.getIntOr("posY", 0),
                compoundTag.getIntOr("posZ", 0)
        )).orElse(BlockPos.ZERO);
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

    public static Climate.ParameterPoint pointFor(ResourceKey<Biome> key) {
        long seed = key.identifier().toString().hashCode();
        seed ^= (seed >>> 33);
        seed *= 0xff51afd7ed558ccdL;
        seed ^= (seed >>> 33);
        seed *= 0xc4ceb9fe1a85ec53L;
        seed ^= (seed >>> 33);

        RandomSource r = RandomSource.create(seed);

        // climate params are generally in [-2, 2], offset in [0, 1]
        float t = r.nextFloat() * 4f - 2f;
        float h = r.nextFloat() * 4f - 2f;
        float c = r.nextFloat() * 4f - 2f;
        float e = r.nextFloat() * 4f - 2f;
        float d = r.nextFloat() * 4f - 2f;
        float w = r.nextFloat() * 4f - 2f;
        float o = r.nextFloat();

        return Climate.parameters(t, h, c, e, d, w, o);
    }

    public enum SpawnDirection {
        NORTH(180),
        EAST(-90),
        SOUTH(0),
        WEST(90);

        public static final Codec<SpawnDirection> CODEC = SkyCodecs.enumCodec(SpawnDirection.class);

        private final int yRot;

        SpawnDirection(int yaw) {
            this.yRot = yaw;
        }

        public static SpawnDirection fromDirection(Direction direction) {
            return switch(direction) {
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
