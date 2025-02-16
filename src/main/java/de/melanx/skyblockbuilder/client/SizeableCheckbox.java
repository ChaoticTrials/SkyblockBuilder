package de.melanx.skyblockbuilder.client;

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

    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_selected_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_selected");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox");

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
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        ResourceLocation resourcelocation;
        if (this.selected()) {
            resourcelocation = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            resourcelocation = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }

        int i = 9;
        guiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), i, i);
    }
}
