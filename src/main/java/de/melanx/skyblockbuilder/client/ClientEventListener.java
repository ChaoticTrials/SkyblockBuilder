package de.melanx.skyblockbuilder.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.melanx.skyblockbuilder.ModBlocks;
import de.melanx.skyblockbuilder.ModDataComponentTypes;
import de.melanx.skyblockbuilder.ModItems;
import de.melanx.skyblockbuilder.Registration;
import de.melanx.skyblockbuilder.commands.OpenDumpScreen;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterPresetEditorsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.lwjgl.glfw.GLFW;
import org.moddingx.libx.render.RenderHelperLevel;

public class ClientEventListener {

    public ClientEventListener() {
        NeoForge.EVENT_BUS.addListener(ClientEventListener::onKeyInput);
        NeoForge.EVENT_BUS.addListener(ClientEventListener::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(ClientEventListener::renderBoundingBox);
    }

    @SubscribeEvent
    public void onRegisterPresetEditors(RegisterPresetEditorsEvent event) {
        event.register(Registration.skyblockKey, ScreenCustomizeSkyblock::new);
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

    private static void renderBoundingBox(RenderLevelStageEvent event) {
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

        ItemStructureSaver.Positions positions = stack.get(ModDataComponentTypes.positions);
        if (positions == null || positions.getPos1() == null || positions.getPos2() == null) {
            return;
        }

        Direction direction = switch (event.getKey()) {
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

        positions.setPos1(positions.getPos1().relative(direction));
        positions.setPos2(positions.getPos2().relative(direction));
    }
}
