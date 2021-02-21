package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.commands.ListCommand;
import de.melanx.skyblockbuilder.commands.TeamCommand;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import de.melanx.skyblockbuilder.util.WorldTypeUtil;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.IslandPos;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

import javax.annotation.Nonnull;
import java.io.IOException;

public class EventListener {

    @SubscribeEvent
    public void resourcesReload(AddReloadListenerEvent event) {
        event.addListener(new ReloadListener<Object>() {
            @Nonnull
            @Override
            protected Object prepare(@Nonnull IResourceManager manager, @Nonnull IProfiler profilerIn) {
                return new Object();
            }

            @Override
            protected void apply(@Nonnull Object unused, @Nonnull IResourceManager manager, @Nonnull IProfiler profiler) {
                try {
                    TemplateLoader.loadSchematic(manager);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("skyblock")
                .then(TeamCommand.register())
                .then(ListCommand.register()));
    }

    /*
     * Mainly taken from Botania
     */
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        World world = event.getPlayer().world;
        if (WorldUtil.isSkyblock(world) && event.getPlayer() instanceof ServerPlayerEntity) {
            SkyblockSavedData data = SkyblockSavedData.get((ServerWorld) world);
            for (Team team : data.skyblocks.values()) {
                if (team.hasPlayer(event.getPlayer())) {
                    return;
                }
            }

            IslandPos islandPos = data.getSpawn();
            ((ServerWorld) world).func_241124_a__(islandPos.getCenter(), 0);
            WorldUtil.teleportToIsland((ServerPlayerEntity) event.getPlayer(), islandPos);
        }
    }

    @SubscribeEvent
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        if (event.getServer() instanceof DedicatedServer) {
            WorldTypeUtil.setupForDedicatedServer((DedicatedServer) event.getServer());
        }
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        if (WorldUtil.isSkyblock(event.getServer().func_241755_D_())) {
            SkyblockSavedData.get(event.getServer().func_241755_D_()).getSpawn();
        }
    }
}
