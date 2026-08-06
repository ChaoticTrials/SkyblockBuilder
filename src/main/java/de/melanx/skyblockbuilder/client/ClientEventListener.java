package de.melanx.skyblockbuilder.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.melanx.skyblockbuilder.client.render.TemplatePreviewPipRenderer;
import de.melanx.skyblockbuilder.client.render.TemplatePreviewRenderState;
import de.melanx.skyblockbuilder.client.screens.CustomizeSkyblockScreen;
import de.melanx.skyblockbuilder.commands.OpenDumpScreen;
import de.melanx.skyblockbuilder.config.common.ClientConfig;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.registration.ModBlocks;
import de.melanx.skyblockbuilder.registration.ModDataComponentTypes;
import de.melanx.skyblockbuilder.registration.ModItems;
import de.melanx.skyblockbuilder.util.NbtUtils;
import de.melanx.skyblockbuilder.world.presets.SkyblockPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class ClientEventListener {

    private static final int BOUNDING_BOX_COLOR = 0xFFE6E6E6;

    public ClientEventListener() {
        NeoForge.EVENT_BUS.addListener(ClientEventListener::onKeyInput);
        NeoForge.EVENT_BUS.addListener(ClientEventListener::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(ClientEventListener::renderBoundingBox);
    }

    @SubscribeEvent
    public void onRegisterPresetEditors(RegisterPresetEditorsEvent event) {
        if (TemplatesConfig.mainSpawnIsland.isPresent() && ModList.get().isLoaded("skyguis") && ClientConfig.removeCustomizeButton) {
            return;
        }

        event.register(SkyblockPreset.KEY, CustomizeSkyblockScreen::new);
    }

    @SubscribeEvent
    public void registerPictureInPictureRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(TemplatePreviewRenderState.class, TemplatePreviewPipRenderer::new);
    }

    @SubscribeEvent
    public void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.structureSaver);
            event.accept(ModBlocks.spawnBlock);
        }
    }

    private static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("skyblock")
                .then(OpenDumpScreen.register())
        );
    }

    private static void renderBoundingBox(ExtractLevelRenderStateEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof ItemStructureSaver)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        BoundingBox area = ItemStructureSaver.getArea(stack);
        if (area == null) {
            return;
        }

        Gizmos.cuboid(AABB.of(area), GizmoStyle.stroke(BOUNDING_BOX_COLOR));
    }

    private static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.structureSaver)) {
            return;
        }

        CompoundTag positions = stack.get(ModDataComponentTypes.positions);
        if (positions == null || !positions.contains("Position1") || !positions.contains("Position2")) {
            return;
        }

        Direction direction = switch(event.getKey()) {
            case GLFW.GLFW_KEY_KP_8 -> player.getDirection();
            case GLFW.GLFW_KEY_KP_2 -> player.getDirection().getOpposite();
            case GLFW.GLFW_KEY_KP_4 -> player.getDirection().getCounterClockWise();
            case GLFW.GLFW_KEY_KP_6 -> player.getDirection().getClockWise();
            case GLFW.GLFW_KEY_KP_9 -> Direction.UP;
            case GLFW.GLFW_KEY_KP_3 -> Direction.DOWN;
            default -> null;
        };

        if (direction == null) {
            return;
        }

        Optional<BlockPos> pos1 = NbtUtils.readBlockPos(positions, "Position1");
        Optional<BlockPos> pos2 = NbtUtils.readBlockPos(positions, "Position2");

        if (pos1.isEmpty() || pos2.isEmpty()) {
            return;
        }

        positions.put("Position1", NbtUtils.writeBlockPos(pos1.get().relative(direction)));
        positions.put("Position2", NbtUtils.writeBlockPos(pos2.get().relative(direction)));
    }
}
