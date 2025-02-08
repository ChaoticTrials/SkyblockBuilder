package de.melanx.skyblockbuilder;

import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.api.SkyblockBuilderAPI;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import de.melanx.skyblockbuilder.commands.*;
import de.melanx.skyblockbuilder.commands.helper.*;
import de.melanx.skyblockbuilder.commands.invitation.AcceptCommand;
import de.melanx.skyblockbuilder.commands.invitation.DeclineCommand;
import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.commands.invitation.JoinCommand;
import de.melanx.skyblockbuilder.commands.operator.GenerateCommand;
import de.melanx.skyblockbuilder.commands.operator.ManageCommand;
import de.melanx.skyblockbuilder.compat.CadmusCompat;
import de.melanx.skyblockbuilder.config.common.*;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.moddingx.libx.event.ConfigLoadedEvent;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@EventBusSubscriber(modid = "skyblockbuilder")
public class EventListener {

    private static final String SPAWNED_TAG = "alreadySpawned";

    @SubscribeEvent
    public static void resourcesReload(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            return;
        }

        SkyPaths.generateDefaultFiles(event.getPlayerList().getServer());
        TemplateLoader.updateTemplates();
        SkyblockBuilder.getNetwork().updateTemplateNames(TemplateLoader.getTemplateNames());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(LocateCommand.register());
        event.getDispatcher().register(Commands.literal("skyblock")
                .requires(source -> SkyblockBuilderAPI.teamManagementEnabled())
                .then(AcceptCommand.register())
                .then(ConvertCommand.register())
                .then(CreateCommand.register())
                .then(DeclineCommand.register())
                .then(GenerateCommand.register())
                .then(HomeCommand.register())
                .then(InventoryCommand.register())
                .then(InviteCommand.register())
                .then(JoinCommand.register())
                .then(LeaveCommand.register())
                .then(ListCommand.register())
                .then(ManageCommand.register())
                .then(SpawnCommand.register())
                .then(SpawnsCommand.register())
                .then(TeamCommand.register())
                .then(VisitCommand.register())
        );

        if (ModList.get().isLoaded(CadmusCompat.MODID)) {
            event.getDispatcher().register(CadmusCompat.spawnProtectionCommand());
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LightningBolt lightning && WorldUtil.isSkyblock(event.getLevel())) {
            ServerLevel level = (ServerLevel) event.getLevel();
            BlockPos pos = new BlockPos((int) lightning.position().x, level.getSeaLevel(), (int) lightning.position().z);
            Optional<BlockPos> rodPos = level.findLightningRod(pos);
            rodPos.ifPresent(blockPos -> lightning.moveTo(Vec3.atBottomCenterOf(blockPos)));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        SkyblockBuilder.getNetwork().updateData(event.getEntity(), null);
        Level level = event.getEntity().level();
        SkyblockBuilder.getNetwork().updateProfiles(level);
        SkyblockBuilder.getNetwork().updateTemplateNames(event.getEntity(), TemplateLoader.getTemplateNames());
        if (level instanceof ServerLevel && WorldUtil.isSkyblock(level) && SkyblockBuilderAPI.isSpawnTeleportEnabled()) {
            SkyblockSavedData data = SkyblockSavedData.get(level);
            ServerPlayer player = (ServerPlayer) event.getEntity();
            Team spawn = data.getSpawn();
            GameProfileCache.addProfiles(Set.of(player.getGameProfile()));
            if (player.getPersistentData().getBoolean(SPAWNED_TAG)) {
                if (!data.hasPlayerTeam(player) && !spawn.hasPlayer(player)) {
                    if (InventoryConfig.dropItems) {
                        RandomUtility.dropInventories(player);
                    }

                    WorldUtil.teleportToIsland(player, spawn);
                    data.addPlayerToTeam(SkyblockSavedData.SPAWN_ID, player);
                    SkyblockBuilder.getLogger().info("Put {} back into spawn team.", player.getDisplayName());
                }

                return;
            }
            player.getPersistentData().putBoolean(SPAWNED_TAG, true);
            data.getOrCreateMetaInfo(player);

            if (InventoryConfig.clearInv) {
                player.getInventory().clearContent();
            }

            SkyblockBuilder.getLogger().info("First time {} joined. Putting into spawn team.", player.getDisplayName());
            data.addPlayerToTeam(spawn, player);
            try {
                //noinspection OptionalGetWithoutIsPresent
                TemplatesConfig.Spawn spawnPos = !spawn.getDefaultPossibleSpawns().isEmpty() ?
                        spawn.getDefaultPossibleSpawns().stream().findFirst().get() :
                        spawn.getPossibleSpawns().stream().findFirst().get();
                ((ServerLevel) level).setDefaultSpawnPos(spawnPos.pos(), spawnPos.direction().getYRot());
            } catch (NoSuchElementException e) {
                throw new IllegalStateException("No possible spawn point set for spawn", e);
            }

            WorldUtil.teleportToIsland(player, spawn);
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        Player newPlayer = event.getEntity();
        CompoundTag newData = newPlayer.getPersistentData();

        Player oldPlayer = event.getOriginal();
        CompoundTag oldData = oldPlayer.getPersistentData();

        newData.putBoolean(SPAWNED_TAG, oldData.getBoolean(SPAWNED_TAG));
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            BlockPos pos = player.getRespawnPosition();

            ServerLevel level = (ServerLevel) player.level();

            if (!WorldUtil.isSkyblock(level)) {
                return;
            }

            if (pos == null || !level.getBlockState(pos).is(BlockTags.BEDS) && !level.getBlockState(pos).is(Blocks.RESPAWN_ANCHOR)) {
                SkyblockSavedData data = SkyblockSavedData.get(level);
                Team team = data.getTeamFromPlayer(player);
                WorldUtil.teleportToIsland(player, team == null ? data.getSpawn() : team);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        SkyPaths.generateDefaultFiles(server);

        if (WorldUtil.isSkyblock(server.overworld())) {
            SkyblockBuilder.getLogger().info("Successfully loaded Skyblock!");
            TemplateLoader.updateTemplates();
            SkyblockBuilder.getNetwork().updateTemplateNames(TemplateLoader.getTemplateNames());
            TemplateData.get(server.overworld());

            Set<GameProfile> profiles = RandomUtility.getGameProfiles(server.overworld());
            GameProfileCache.addProfiles(profiles);

            if (SkyblockBuilderAPI.isSpawnTeleportEnabled()) {
                SkyblockSavedData.get(server.overworld()).getSpawn();
            }
        }
    }

    @SubscribeEvent
    public static void onTabListName(PlayerEvent.TabListNameFormat event) {
        if (!CustomizationConfig.showTeamInTabList) {
            return;
        }

        Player player = event.getEntity();
        Team team = SkyblockSavedData.get(player.level()).getTeamFromPlayer(player);
        if (team != null) {
            MutableComponent name = (MutableComponent) player.getDisplayName();
            Style style = name.getStyle();
            if (style.getColor() == null) {
                style.withColor(ChatFormatting.WHITE);
            }
            MutableComponent teamName = Component.literal(team.getName()).withStyle(ChatFormatting.AQUA);
            event.setDisplayName(Component.empty()
                    .append(Component.literal("[").withStyle(style))
                    .append(teamName.withStyle(ChatFormatting.AQUA))
                    .append(Component.literal("] ").withStyle(style))
                    .append(name.withStyle(style))
            );
        } else {
            event.setDisplayName(null);
        }
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigLoadedEvent event) {
        if (event.getConfigClass() == TemplatesConfig.class && event.getReason() != ConfigLoadedEvent.LoadReason.SHADOW) {
            TemplateLoader.updateTemplates();
        }

        if (event.getConfigClass() == PermissionsConfig.class) {
            if (PermissionsConfig.forceSkyblockCheck) {
                SkyblockBuilder.getLogger().warn("'forceSkyblockCheck' is enabled");
            }
        }

        if (event.getConfigClass() == WorldConfig.class || event.getConfigClass() == DimensionsConfig.class || event.getConfigClass() == SpawnConfig.class) {
            int overworldCenterBiomesRadius = DimensionsConfig.Overworld.centeredBiomes.stream()
                    .mapToInt(DimensionsConfig.UnregisteredCenterBiome::radius)
                    .sum();

            int netherCenterBiomesRadius = DimensionsConfig.Nether.centeredBiomes.stream()
                    .mapToInt(DimensionsConfig.UnregisteredCenterBiome::radius)
                    .sum();

            if (SpawnConfig.dimension == Level.OVERWORLD && overworldCenterBiomesRadius > WorldConfig.islandDistance) {
                SkyblockBuilder.getLogger().warn("The overworld center biomes radius is higher than the island distance. This will result in unwanted behaviour.");
            }

            if (SpawnConfig.dimension == Level.NETHER && netherCenterBiomesRadius > WorldConfig.islandDistance) {
                SkyblockBuilder.getLogger().warn("The nether center biomes radius is higher than the island distance. This will result in unwanted behaviour.");
            }
        }
    }
}
