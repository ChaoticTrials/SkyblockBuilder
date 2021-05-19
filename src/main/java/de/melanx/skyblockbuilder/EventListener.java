package de.melanx.skyblockbuilder;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import de.melanx.skyblockbuilder.commands.*;
import de.melanx.skyblockbuilder.commands.helper.ListCommand;
import de.melanx.skyblockbuilder.commands.helper.SpawnsCommand;
import de.melanx.skyblockbuilder.commands.invitation.AcceptCommand;
import de.melanx.skyblockbuilder.commands.invitation.DeclineCommand;
import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.commands.invitation.JoinCommand;
import de.melanx.skyblockbuilder.commands.operator.ManageCommand;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.config.LibXConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.util.*;
import io.github.noeppi_noeppi.libx.event.DatapacksReloadedEvent;
import io.github.noeppi_noeppi.libx.render.RenderHelperWorld;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

@Mod.EventBusSubscriber(modid = "skyblockbuilder")
public class EventListener {

    private static final String SPAWNED_TAG = "alreadySpawned";

    @SubscribeEvent
    public static void resourcesReload(DatapacksReloadedEvent event) {
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

    /*
     * Mainly taken from Botania
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        World world = event.getPlayer().world;
        if (world instanceof ServerWorld && WorldUtil.isSkyblock(world) && CompatHelper.isSpawnTeleportEnabled()) {
            if (LibXConfigHandler._reminder) {
                event.getPlayer().sendStatusMessage(new StringTextComponent("[Skyblock Builder] The config system for this mod changed. " +
                        "It now uses LibX and its config system. All your current configs were transferred to the new one. " +
                        "You should only change the new config from now. You find it at config/skyblockbuilder/common-config.json5. " +
                        "You can disable this annoying message in the config.").mergeStyle(TextFormatting.RED), false);
            }

            SkyblockSavedData data = SkyblockSavedData.get((ServerWorld) world);
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            Team spawn = data.getSpawn();
            if (player.getPersistentData().getBoolean(SPAWNED_TAG)) {
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

            if (ConfigHandler.clearInv.get()) {
                player.inventory.clear();
            }

            ConfigHandler.getStarterItems().forEach(entry -> {
                if (entry.getLeft() == EquipmentSlotType.MAINHAND) {
                    player.inventory.addItemStackToInventory(entry.getRight().copy());
                } else {
                    player.setItemStackToSlot(entry.getLeft(), entry.getRight().copy());
                }
            });

            data.addPlayerToTeam(spawn, player);
            ((ServerWorld) world).func_241124_a__(spawn.getIsland().getCenter(), ConfigHandler.direction.get().getYaw());
            WorldUtil.teleportToIsland(player, spawn);
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        PlayerEntity newPlayer = event.getPlayer();
        CompoundNBT newData = newPlayer.getPersistentData();

        PlayerEntity oldPlayer = event.getOriginal();
        CompoundNBT oldData = oldPlayer.getPersistentData();

        newData.putBoolean(SPAWNED_TAG, oldData.getBoolean(SPAWNED_TAG));
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getPlayer().world.isRemote) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            BlockPos pos = player.func_241140_K_();

            ServerWorld world = player.getServerWorld();
            if (pos == null || !world.getBlockState(pos).isIn(BlockTags.BEDS) && !world.getBlockState(pos).isIn(Blocks.RESPAWN_ANCHOR)) {
                SkyblockSavedData data = SkyblockSavedData.get(world);
                Team team = data.getTeamFromPlayer(player);
                WorldUtil.teleportToIsland(player, team == null ? data.getSpawn() : team);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStarted(FMLServerStartedEvent event) {
        RandomUtility.dynamicRegistries = event.getServer().func_244267_aX();
        if (WorldUtil.isSkyblock(event.getServer().func_241755_D_())) {
            SkyPaths.generateDefaultFiles();
            TemplateLoader.updateTemplates();
            TemplateData.get(event.getServer().func_241755_D_());

            if (CompatHelper.isSpawnTeleportEnabled()) {
                SkyblockSavedData.get(event.getServer().func_241755_D_()).getSpawn();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderBoundingBox(RenderWorldLastEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null || !(player.getHeldItemMainhand().getItem() instanceof ItemStructureSaver)) {
            return;
        }

        ItemStack stack = player.getHeldItemMainhand();
        MutableBoundingBox area = ItemStructureSaver.getArea(stack);
        if (area == null) {
            return;
        }

        MatrixStack matrixStack = event.getMatrixStack();
        matrixStack.push();
        RenderHelperWorld.loadProjection(matrixStack, area.minX, area.minY, area.minZ);

        IRenderTypeBuffer.Impl source = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder buffer = source.getBuffer(RenderType.LINES);

        WorldRenderer.drawBoundingBox(matrixStack, buffer, 0, 0, 0, area.maxX - area.minX + 1, area.maxY - area.minY + 1, area.maxZ - area.minZ + 1, 0.9F, 0.9F, 0.9F, 1.0F);
        source.finish(RenderType.LINES);
        matrixStack.pop();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onChangeScreen(GuiScreenEvent.DrawScreenEvent event) {
        if (event.getGui() instanceof WorldSelectionScreen) {
            TemplateLoader.loadSchematic();
        }
    }
}
