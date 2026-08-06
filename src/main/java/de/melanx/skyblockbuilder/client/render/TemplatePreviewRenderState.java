package de.melanx.skyblockbuilder.client.render;

import de.melanx.skyblockbuilder.template.TemplatePreviewRenderer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record TemplatePreviewRenderState(
        TemplatePreviewRenderer renderer,
        int x0,
        int y0,
        int x1,
        int y1,
        float scale,
        Matrix3x2f pose,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {

    public TemplatePreviewRenderState(TemplatePreviewRenderer renderer, int x0, int y0, int x1, int y1, float scale, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        this(renderer, x0, y0, x1, y1, scale, pose, scissorArea, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }
}
