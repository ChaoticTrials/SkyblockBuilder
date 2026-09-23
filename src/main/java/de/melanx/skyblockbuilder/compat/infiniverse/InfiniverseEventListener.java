package de.melanx.skyblockbuilder.compat.infiniverse;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.commoble.infiniverse.api.UnregisterDimensionEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

public class InfiniverseEventListener {

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!InfiniverseCompat.useInfiniverse()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        SkyblockSavedData data = SkyblockSavedData.get(player.level());
        Team team = data.getTeamFromPlayer(player);
        if (team != null) {
            team.setLastSeen(player.level().getServer().overworld().getGameTime());
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (!InfiniverseCompat.useInfiniverse()) {
            return;
        }

        MinecraftServer server = event.getServer();
        if (server.getTickCount() % WorldConfig.DimensionPerTeam.unloadCheckInterval != 0) {
            return;
        }

        if (WorldUtil.isSkyblock(server.overworld())) {
            SkyblockSavedData.get(server.overworld()).unloadIdleDimensions();
        }
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (!InfiniverseCompat.useInfiniverse()) {
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ResourceKey<Level> key = level.dimension();
        MinecraftServer server = level.getServer();

        if (!InfiniverseCompat.getLevelsToProcessAfterDeletion().contains(key)) {
            return;
        }

        Path dimPath = server.storageSource.getDimensionPath(key);

        if (!Files.exists(dimPath)) {
            return;
        }

        try {
            switch(WorldConfig.DimensionPerTeam.deletedDimensionHandling) {
                case MOVE -> {
                    Path root = server.storageSource.getLevelPath(LevelResource.ROOT);
                    Path target = root.resolve("deleted_dimensions")
                            .resolve(key.identifier().getNamespace())
                            .resolve(key.identifier().getPath());
                    Files.createDirectories(target.getParent());
                    Files.move(dimPath, target, StandardCopyOption.REPLACE_EXISTING);
                }
                case DELETE -> {
                    try (Stream<Path> walk = Files.walk(dimPath)) {
                        walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                SkyblockBuilder.getLogger().error("Failed to delete {}", path, e);
                            }
                        });
                    }
                }
            }
        } catch (IOException e) {
            SkyblockBuilder.getLogger().error("Failed to {} dimension folder for {}", WorldConfig.DimensionPerTeam.deletedDimensionHandling, key.identifier(), e);
        } finally {
            InfiniverseCompat.levelProcessed(key);
        }
    }

    @SubscribeEvent
    public void onUnregisterDimension(UnregisterDimensionEvent event) {
        ServerLevel level = event.getLevel();
        if (InfiniverseCompat.getLevelsToProcessAfterDeletion().contains(level.dimension())) {
            level.noSave = true;
        }
    }
}
