package de.melanx.skyblockbuilder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.melanx.skyblockbuilder.commands.*;
import de.melanx.skyblockbuilder.commands.helper.ListCommand;
import de.melanx.skyblockbuilder.commands.helper.SpawnsCommand;
import de.melanx.skyblockbuilder.commands.invitation.AcceptCommand;
import de.melanx.skyblockbuilder.commands.invitation.DeclineCommand;
import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.commands.invitation.JoinCommand;
import de.melanx.skyblockbuilder.commands.operator.ManageCommand;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.util.*;
import io.github.noeppi_noeppi.libx.event.DataPacksReloadedEvent;
import io.github.noeppi_noeppi.libx.render.RenderHelperLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmlserverevents.FMLServerStartedEvent;

@Mod.EventBusSubscriber(modid = "skyblockbuilder")
public class EventListener {

    private static final String SPAWNED_TAG = "alreadySpawned";

    @SubscribeEvent
    public static void resourcesReload(DataPacksReloadedEvent event) {
        SkyPaths.generateDefaultFiles();
        TemplateLoader.updateTemplates();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("skyblock")
                .requires(source -> CompatHelper.teamManagementEnabled())
                .then(AcceptCommand.register())
                .then(CreateCommand.register())
                .then(DeclineCommand.register())
                .then(HomeCommand.register())
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

    // Mainly taken from Botania
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Level level = event.getPlayer().level;
        if (level instanceof ServerLevel && WorldUtil.isSkyblock(level) && CompatHelper.isSpawnTeleportEnabled()) {

            SkyblockSavedData data = SkyblockSavedData.get((ServerLevel) level);
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            Team spawn = data.getSpawn();
            if (player.getPersistentData().getBoolean(SPAWNED_TAG)) {
                if (!data.hasPlayerTeam(player) && !data.getSpawn().hasPlayer(player)) {
                    if (ConfigHandler.Inventory.dropItems) {
                        player.getInventory().dropAll();
                    }

                    WorldUtil.teleportToIsland(player, spawn);
                    data.addPlayerToTeam("spawn", player);
                }

                return;
            }
            player.getPersistentData().putBoolean(SPAWNED_TAG, true);

            if (ConfigHandler.Inventory.clearInv) {
                player.getInventory().clearContent();
            }

            ConfigHandler.getStarterItems().forEach(entry -> {
                if (entry.getLeft() == EquipmentSlot.MAINHAND) {
                    player.getInventory().add(entry.getRight().copy());
                } else {
                    player.setItemSlot(entry.getLeft(), entry.getRight().copy());
                }
            });

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
            if (pos == null || !level.getBlockState(pos).is(BlockTags.BEDS) && !level.getBlockState(pos).is(Blocks.RESPAWN_ANCHOR)) {
                SkyblockSavedData data = SkyblockSavedData.get(level);
                Team team = data.getTeamFromPlayer(player);
                WorldUtil.teleportToIsland(player, team == null ? data.getSpawn() : team);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStarted(FMLServerStartedEvent event) {
        RandomUtility.dynamicRegistries = event.getServer().registryAccess();
        if (WorldUtil.isSkyblock(event.getServer().overworld())) {
            SkyPaths.generateDefaultFiles();
            TemplateLoader.updateTemplates();
            TemplateData.get(event.getServer().overworld());

            if (CompatHelper.isSpawnTeleportEnabled()) {
                SkyblockSavedData.get(event.getServer().overworld()).getSpawn();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderBoundingBox(RenderWorldLastEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof ItemStructureSaver)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        BoundingBox area = ItemStructureSaver.getArea(stack);
        if (area == null) {
            return;
        }

        PoseStack poseStack = event.getMatrixStack();
        poseStack.pushPose();
        RenderHelperLevel.loadProjection(poseStack, area.minX(), area.minY(), area.minZ());

        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer buffer = source.getBuffer(RenderType.LINES);

        LevelRenderer.renderLineBox(poseStack, buffer, 0, 0, 0, area.maxX() - area.minX() + 1, area.maxY() - area.minY() + 1, area.maxZ() - area.minZ() + 1, 0.9F, 0.9F, 0.9F, 1.0F);
        source.endBatch(RenderType.LINES);
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onChangeScreen(GuiScreenEvent.DrawScreenEvent event) {
        if (event.getGui() instanceof SelectWorldScreen) {
            TemplateLoader.loadSchematic();
        }
    }
}
