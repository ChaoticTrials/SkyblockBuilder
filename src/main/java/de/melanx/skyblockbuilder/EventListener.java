package de.melanx.skyblockbuilder;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.melanx.skyblockbuilder.api.SkyblockBuilderAPI;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import de.melanx.skyblockbuilder.commands.*;
import de.melanx.skyblockbuilder.commands.helper.InventoryCommand;
import de.melanx.skyblockbuilder.commands.helper.ListCommand;
import de.melanx.skyblockbuilder.commands.helper.SpawnsCommand;
import de.melanx.skyblockbuilder.commands.invitation.AcceptCommand;
import de.melanx.skyblockbuilder.commands.invitation.DeclineCommand;
import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.commands.invitation.JoinCommand;
import de.melanx.skyblockbuilder.commands.operator.ManageCommand;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.config.TemplateConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.WorldUtil;
import io.github.noeppi_noeppi.libx.event.ConfigLoadedEvent;
import io.github.noeppi_noeppi.libx.render.RenderHelperLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = "skyblockbuilder")
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
        event.getDispatcher().register(Commands.literal("skyblock")
                .requires(source -> SkyblockBuilderAPI.teamManagementEnabled())
                .then(AcceptCommand.register())
                .then(CreateCommand.register())
                .then(DeclineCommand.register())
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
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        SkyblockBuilder.getNetwork().updateData(event.getPlayer());
        Level level = event.getPlayer().level;
        SkyblockBuilder.getNetwork().updateProfiles(level);
        SkyblockBuilder.getNetwork().updateTemplateNames(event.getPlayer(), TemplateLoader.getTemplateNames());
//        if (level instanceof ServerLevel) { TODO remove
//            RegistryOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, level.registryAccess());
//            Optional<JsonElement> result = ChunkGenerator.CODEC.encodeStart(dynamicOps, ((ServerLevel) level).getChunkSource().getGenerator()).result();
//            result.ifPresent(jsonElement -> SkyblockBuilder.getLogger().info(jsonElement.toString()));
//        }
        if (level instanceof ServerLevel && WorldUtil.isSkyblock(level) && SkyblockBuilderAPI.isSpawnTeleportEnabled()) {
            SkyblockSavedData data = SkyblockSavedData.get(level);
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            Team spawn = data.getSpawn();
            GameProfileCache.addProfiles(Set.of(player.getGameProfile()));
            if (player.getPersistentData().getBoolean(SPAWNED_TAG)) {
                if (!data.hasPlayerTeam(player) && !spawn.hasPlayer(player)) {
                    if (ConfigHandler.Inventory.dropItems) {
                        RandomUtility.dropInventories(player);
                    }

                    WorldUtil.teleportToIsland(player, spawn);
                    data.addPlayerToTeam(SkyblockSavedData.SPAWN_ID, player);
                }

                return;
            }
            player.getPersistentData().putBoolean(SPAWNED_TAG, true);
            data.addMetaInfo(player);

            if (ConfigHandler.Inventory.clearInv) {
                player.getInventory().clearContent();
            }

            data.addPlayerToTeam(spawn, player);
            ((ServerLevel) level).setDefaultSpawnPos(spawn.getIsland().getCenter(), ConfigHandler.Spawn.direction.getYRot());
            WorldUtil.teleportToIsland(player, spawn);
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        Player newPlayer = event.getPlayer();
        CompoundTag newData = newPlayer.getPersistentData();

        Player oldPlayer = event.getOriginal();
        CompoundTag oldData = oldPlayer.getPersistentData();

        newData.putBoolean(SPAWNED_TAG, oldData.getBoolean(SPAWNED_TAG));
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getPlayer().level.isClientSide) {
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            BlockPos pos = player.getRespawnPosition();

            ServerLevel level = player.getLevel();

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
        RandomUtility.dynamicRegistries = server.registryAccess();
        if (WorldUtil.isSkyblock(server.overworld())) {
            SkyPaths.generateDefaultFiles(server);
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderBoundingBox(RenderLevelLastEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof ItemStructureSaver)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        BoundingBox area = ItemStructureSaver.getArea(stack);
        if (area == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        RenderHelperLevel.loadProjection(poseStack, area.minX(), area.minY(), area.minZ());

        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer buffer = source.getBuffer(RenderType.LINES);

        LevelRenderer.renderLineBox(poseStack, buffer, 0, 0, 0, area.maxX() - area.minX() + 1, area.maxY() - area.minY() + 1, area.maxZ() - area.minZ() + 1, 0.9F, 0.9F, 0.9F, 1.0F);
        source.endBatch(RenderType.LINES);
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigLoadedEvent event) {
        if (event.getConfigClass() == TemplateConfig.class
                && event.getReason() != ConfigLoadedEvent.LoadReason.SHADOW) {
            TemplateLoader.updateTemplates();
        }
    }
}
