package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.commands.*;
import de.melanx.skyblockbuilder.commands.helper.ListCommand;
import de.melanx.skyblockbuilder.commands.helper.SpawnsCommand;
import de.melanx.skyblockbuilder.commands.invitation.AcceptCommand;
import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.commands.operator.ManageCommand;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import de.melanx.skyblockbuilder.util.WorldTypeUtil;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

import javax.annotation.Nonnull;
import java.io.IOException;

public class EventListener {
    private static final String SPAWNED_TAG = "alreadySpawned";

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
                    ConfigHandler.generateDefaultFiles();
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
                .requires(source -> WorldUtil.isSkyblock(source.getWorld()))
                .then(AcceptCommand.register())
                .then(CreateCommand.register())
                .then(HomeCommand.register())
                .then(InviteCommand.register())
                .then(LeaveCommand.register())
                .then(ListCommand.register())
                .then(ManageCommand.register())
                .then(SpawnCommand.register())
                .then(SpawnsCommand.register())
                .then(TeamCommand.register())
                .then(VisitCommand.register()));
    }

    /*
     * Mainly taken from Botania
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        World world = event.getPlayer().world;
        if (world instanceof ServerWorld) {
            if (WorldUtil.isSkyblock(world)) {
                SkyblockSavedData data = SkyblockSavedData.get((ServerWorld) world);
                ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
                Team spawn = data.getSpawn();
                if (player.getPersistentData().getBoolean(SPAWNED_TAG)) {
                    Team team = data.getTeamFromPlayer(player);
                    if (!data.hasPlayerTeam(player) && !data.getSpawn().hasPlayer(player)) {
                        if (ConfigHandler.dropItems.get()) {
                            player.inventory.dropAllItems();
                        }
                        WorldUtil.teleportToIsland(player, spawn);
                        data.addPlayerToTeam("spawn", player);
                    }

                    return;
                }

                player.getPersistentData().putBoolean(SPAWNED_TAG, true);
                spawn.addPlayer(player);
                ((ServerWorld) world).func_241124_a__(spawn.getIsland().getCenter(), ConfigHandler.direction.get().getYaw());
                WorldUtil.teleportToIsland(player, spawn);

                if (ConfigHandler.clearInv.get()) {
                    player.inventory.clear();
                }

                ConfigHandler.STARTER_ITEMS.forEach(player.inventory::addItemStackToInventory);
            }
        }
    }

    @SubscribeEvent
    public void clonePlayer(PlayerEvent.Clone event) {
        PlayerEntity newPlayer = event.getPlayer();
        CompoundNBT newData = newPlayer.getPersistentData();

        PlayerEntity oldPlayer = event.getOriginal();
        CompoundNBT oldData = oldPlayer.getPersistentData();

        newData.putBoolean(SPAWNED_TAG, oldData.getBoolean(SPAWNED_TAG));
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
