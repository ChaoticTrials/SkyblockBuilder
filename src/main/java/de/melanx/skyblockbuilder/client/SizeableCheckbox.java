package de.melanx.skyblockbuilder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SizeableCheckbox extends Checkbox {

    private static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected_highlighted");
    private static final Identifier CHECKBOX_SELECTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected");
    private static final Identifier CHECKBOX_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_highlighted");
    private static final Identifier CHECKBOX_SPRITE = Identifier.withDefaultNamespace("widget/checkbox");

    public SizeableCheckbox(int x, int y, int size, boolean selected) {
        this(x, y, size, selected, (_, _) -> {});
    }

    public SizeableCheckbox(int x, int y, int size, boolean selected, Checkbox.OnValueChange onValueChange) {
        this(x, y, size, selected, (Tooltip) null, onValueChange);
    }

    public SizeableCheckbox(int x, int y, int size, boolean selected, Component component, Checkbox.OnValueChange onValueChange) {
        this(x, y, size, selected, Tooltip.create(component), onValueChange);
    }

    public SizeableCheckbox(int x, int y, int size, boolean selected, @Nullable Tooltip tooltip, Checkbox.OnValueChange onValueChange) {
        super(x, y, size, Component.empty(), Minecraft.getInstance().font, selected, onValueChange);
        this.height = size;
        this.setTooltip(tooltip);
    }


    @Override
    public void extractContents(@Nonnull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        Identifier identifier;
        if (this.selected()) {
            identifier = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            identifier = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }

        int i = 9;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), i, i, ARGB.white(this.alpha));
    }
}
