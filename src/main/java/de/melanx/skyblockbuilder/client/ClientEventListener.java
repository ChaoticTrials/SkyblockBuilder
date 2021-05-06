package de.melanx.skyblockbuilder.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import io.github.noeppi_noeppi.libx.render.RenderHelperWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "skyblockbuilder", value = Dist.CLIENT)
public class ClientEventListener {

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

    @SubscribeEvent
    public static void onChangeScreen(GuiScreenEvent.DrawScreenEvent event) {
        if (event.getGui() instanceof WorldSelectionScreen) {
            TemplateLoader.loadSchematic();
        }
    }
}
