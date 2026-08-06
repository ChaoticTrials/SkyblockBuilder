package de.melanx.skyblockbuilder.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;

import javax.annotation.Nonnull;

public class TemplatePreviewPipRenderer extends PictureInPictureRenderer<TemplatePreviewRenderState> {

    public TemplatePreviewPipRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Nonnull
    @Override
    public Class<TemplatePreviewRenderState> getRenderStateClass() {
        return TemplatePreviewRenderState.class;
    }

    @Override
    protected void renderToTexture(TemplatePreviewRenderState renderState, @Nonnull PoseStack poseStack) {
        FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        renderState.renderer().renderTemplate(poseStack, featureRenderDispatcher.getSubmitNodeStorage());
        featureRenderDispatcher.renderAllFeatures();
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2F;
    }

    @Nonnull
    @Override
    protected String getTextureLabel() {
        return "skyblockbuilder template preview";
    }
}
