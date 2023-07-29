package de.melanx.skyblockbuilder;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.melanx.skyblockbuilder.api.SkyblockBuilderAPI;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import de.melanx.skyblockbuilder.commands.*;
import de.melanx.skyblockbuilder.commands.helper.*;
import de.melanx.skyblockbuilder.commands.invitation.AcceptCommand;
import de.melanx.skyblockbuilder.commands.invitation.DeclineCommand;
import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.commands.invitation.JoinCommand;
import de.melanx.skyblockbuilder.commands.operator.ManageCommand;
import de.melanx.skyblockbuilder.config.StartingInventory;
import de.melanx.skyblockbuilder.config.common.InventoryConfig;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyPaths;
import de.melanx.skyblockbuilder.util.WorldUtil;
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
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.moddingx.libx.event.ConfigLoadedEvent;
import org.moddingx.libx.render.RenderHelperLevel;

import java.util.Optional;
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
                .then(ConvertCommand.register())
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
                .then(TemplatesToSnbtCommand.register())
                .then(VisitCommand.register())
        );
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
                }

                return;
            }
            player.getPersistentData().putBoolean(SPAWNED_TAG, true);
            data.getOrCreateMetaInfo(player);

            if (InventoryConfig.clearInv) {
                player.getInventory().clearContent();
            }

            data.addPlayerToTeam(spawn, player);
            //noinspection OptionalGetWithoutIsPresent
            WorldUtil.Directions direction = spawn.getDefaultPossibleSpawns().stream().findFirst().get().direction();
            ((ServerLevel) level).setDefaultSpawnPos(spawn.getIsland().getCenter(), direction.getYRot());
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
    public static void renderBoundingBox(RenderLevelStageEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof ItemStructureSaver) || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        BoundingBox area = ItemStructureSaver.getArea(stack);
        if (area == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        RenderHelperLevel.loadCameraPosition(event.getCamera(), poseStack, area.minX(), area.minY(), area.minZ());

        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer buffer = source.getBuffer(RenderType.LINES);

        LevelRenderer.renderLineBox(poseStack, buffer, 0, 0, 0, area.maxX() - area.minX() + 1, area.maxY() - area.minY() + 1, area.maxZ() - area.minZ() + 1, 0.9F, 0.9F, 0.9F, 1.0F);
        source.endBatch(RenderType.LINES);
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigLoadedEvent event) {
        if (event.getConfigClass() == TemplatesConfig.class) {
            StartingInventory.loadStarterItems();
            if (event.getReason() != ConfigLoadedEvent.LoadReason.SHADOW) {
                TemplateLoader.updateTemplates();
            }
        }
    }
}
