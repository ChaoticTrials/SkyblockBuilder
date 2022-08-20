package de.melanx.skyblockbuilder.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.ClientUtility;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;

public class ScreenStructureSaver extends Screen {

    private static final ResourceLocation SCREEN_LOCATION = new ResourceLocation(SkyblockBuilder.getInstance().modid, "textures/gui/structure_saver.png");

    private final int xSize;
    private final int ySize;
    private final ItemStack stack;
    private int relX;
    private int relY;
    private EditBox name;
    private Checkbox ignoreAir;
    private Checkbox nbtToSnbt;

    public ScreenStructureSaver(ItemStack stack, Component title) {
        super(title);
        this.xSize = 196;
        this.ySize = 85;
        this.stack = stack;
    }

    @Override
    protected void init() {
        this.relX = (this.width - this.xSize) / 2;
        this.relY = (this.height - this.ySize) / 2;
        this.name = new EditBox(this.font, this.relX + 11, this.relY + 25, 125, 17, Component.translatable("skyblockbuilder.screen.widget.structure_name"));
        this.name.setMaxLength(Short.MAX_VALUE);
        this.name.changeFocus(true);
        this.name.setValue(this.name.getValue());
        this.addRenderableWidget(new Button(this.relX + 10, this.relY + 55, 60, 20, Component.translatable("skyblockbuilder.screen.button.save"), button -> {
            SkyblockBuilder.getNetwork().saveStructure(this.stack, this.name.getValue().isEmpty() ? "template" : this.name.getValue(), this.ignoreAir.selected(), this.nbtToSnbt.selected());
            this.onClose();
        }));
        this.addRenderableWidget(new Button(this.relX + 77, this.relY + 55, 60, 20, Component.translatable("skyblockbuilder.screen.button.delete"), button -> {
            SkyblockBuilder.getNetwork().deleteTags(this.stack);
            this.onClose();
        }));
        this.addRenderableWidget(new Button(this.relX + 144, this.relY + 23, 20, 20, Component.empty(), button -> {
            ClientUtility.openPath("skyblock_exports");
        }, (button, poseStack, mouseX, mouseY) -> this.renderTooltip(poseStack, Component.translatable("skyblockbuilder.screen.button.open_folder.tooltip"), mouseX, mouseY)));
        this.ignoreAir = this.addRenderableWidget(new Checkbox(this.relX + 144, this.relY + 55, 20, 20, Component.empty(), false, false));
        this.nbtToSnbt = this.addRenderableWidget(new Checkbox(this.relX + 169, this.relY + 55, 20, 20, Component.empty(), false, false));
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, SCREEN_LOCATION);
        this.blit(poseStack, this.relX, this.relY, 0, 0, this.xSize, this.ySize);

        this.name.render(poseStack, mouseX, mouseY, partialTick);
        this.font.draw(poseStack, this.title, this.relX + 10, this.relY + 8, Color.DARK_GRAY.getRGB());
        super.render(poseStack, mouseX, mouseY, partialTick);

        RenderSystem.setShaderTexture(0, SCREEN_LOCATION);
        this.blit(poseStack, this.relX + 147, this.relY + 25, 0, this.ySize, 14, 14);

        if (this.ignoreAir.isHoveredOrFocused()) {
            this.renderTooltip(poseStack, Component.translatable("skyblockbuilder.item.structure_saver.ignore_air.tooltip"), mouseX, mouseY);
        } else if (this.nbtToSnbt.isHoveredOrFocused()) {
            this.renderTooltip(poseStack, Component.translatable("skyblockbuilder.item.structure_saver.nbt_to_snbt.tooltip"), mouseX, mouseY);
        }
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.name;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mapping = InputConstants.getKey(keyCode, scanCode);
        //noinspection ConstantConditions
        if (keyCode != 256 && (this.minecraft.options.keyInventory.isActiveAndMatches(mapping)
                || this.minecraft.options.keyDrop.isActiveAndMatches(mapping))) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        this.name.tick();
        super.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
