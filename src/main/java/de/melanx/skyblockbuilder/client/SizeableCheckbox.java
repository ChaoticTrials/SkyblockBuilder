package de.melanx.skyblockbuilder.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SizeableCheckbox extends Checkbox {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/checkbox.png");

    public SizeableCheckbox(int x, int y, int size, boolean selected) {
        this(x, y, size, selected, (checkbox, value) -> {
        });
    }

    public SizeableCheckbox(int x, int y, int size, boolean selected, Checkbox.OnValueChange onValueChange) {
        this(x, y, size, selected, (Tooltip) null, onValueChange);
    }

    public SizeableCheckbox(int x, int y, int size, boolean selected, Component component, Checkbox.OnValueChange onValueChange) {
        this(x, y, size, selected, Tooltip.create(component), onValueChange);
    }

    public SizeableCheckbox(int x, int y, int size, boolean selected, @Nullable Tooltip tooltip, Checkbox.OnValueChange onValueChange) {
        super(x, y, size, Component.empty(), Minecraft.getInstance().font, selected, onValueChange);
        this.setTooltip(tooltip);
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(this.width / 20f, this.height / 20f, 1);
        guiGraphics.blit(TEXTURE, this.getX() * 20 / this.width, this.getY() * 20 / this.height, this.isFocused() ? 20.0F : 0.0F, this.selected() ? 20.0F : 0.0F, 20, 20, 64, 64);
        guiGraphics.pose().popPose();
    }
}
