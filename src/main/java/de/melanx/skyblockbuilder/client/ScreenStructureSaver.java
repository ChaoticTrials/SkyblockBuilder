package de.melanx.skyblockbuilder.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;

public class ScreenStructureSaver extends BaseScreen {

    private static final ResourceLocation SCREEN_LOCATION = ResourceLocation.fromNamespaceAndPath(SkyblockBuilder.getInstance().modid, "textures/gui/structure_saver.png");
    private static final Component SAVE_TO_CONFIG = Component.translatable("skyblockbuilder.item.structure_saver.save_to_config.tooltip");
    private static final Component IGNORE_AIR = Component.translatable("skyblockbuilder.item.structure_saver.ignore_air.tooltip");
    private static final Component SNBT = Component.translatable("skyblockbuilder.item.structure_saver.nbt_to_snbt.tooltip");
    private static final Component NETHER_VALIDATION = Component.translatable("skyblockbuilder.item.structure_saver.nether_validation.tooltip");
    private static final Component SAVE_TO_CONFIG_DESC = Component.translatable("skyblockbuilder.item.structure_saver.save_to_config.desc");
    private static final Component IGNORE_AIR_DESC = Component.translatable("skyblockbuilder.item.structure_saver.ignore_air.desc");
    private static final Component SNBT_DESC = Component.translatable("skyblockbuilder.item.structure_saver.nbt_to_snbt.desc");
    private static final Component NETHER_VALIDATION_DESC = Component.translatable("skyblockbuilder.item.structure_saver.nether_validation.desc");

    private final ItemStack stack;
    private EditBox name;
    private Checkbox ignoreAir;
    private Checkbox nbtToSnbt;
    private Checkbox netherValidation;
    private Checkbox saveToConfig;

    public ScreenStructureSaver(ItemStack stack, Component title) {
        super(174, 142, title);
        this.stack = stack;
    }

    @Override
    protected void init() {
        super.init();
        this.name = new EditBox(this.font, this.x(11), this.y(25), 125, 17, Component.translatable("skyblockbuilder.screen.widget.structure_name"));
        this.name.setMaxLength(Short.MAX_VALUE);
        this.name.setFocused(true);
        this.name.setValue(this.name.getValue());
        this.addRenderableWidget(Button.builder(Component.translatable("skyblockbuilder.screen.button.save"), button -> {
                    SkyblockBuilder.getNetwork().saveStructure(this.stack, this.name.getValue().isEmpty() ? "template" : this.name.getValue(), this.saveToConfig.selected(), this.ignoreAir.selected(), this.nbtToSnbt.selected(), this.netherValidation.selected());
                    this.onClose();
                })
                .pos(this.x(10), this.y(55))
                .size(60, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("skyblockbuilder.screen.button.delete"), button -> {
                    SkyblockBuilder.getNetwork().deleteTags(this.stack);
                    this.onClose();
                })
                .pos(this.x(77), this.y(55))
                .size(60, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.empty(), button -> {
                    ClientUtil.openPath(SkyPaths.MOD_EXPORTS);
                })
                .pos(this.x(144), this.y(23))
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("skyblockbuilder.screen.button.open_folder.tooltip")))
                .build());
        this.saveToConfig = this.addRenderableWidget(new SizeableCheckbox(this.x(10), this.y(80), 10, false));
        this.ignoreAir = this.addRenderableWidget(new SizeableCheckbox(this.x(10), this.y(95), 10, false));
        this.nbtToSnbt = this.addRenderableWidget(new SizeableCheckbox(this.x(10), this.y(110), 10, false));
        this.netherValidation = this.addRenderableWidget(new SizeableCheckbox(this.x(10), this.y(125), 10, false));
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.blit(SCREEN_LOCATION, this.x(146), this.y(25), 0, 0, 16, 16, 16, 16);
    }

    @Override
    public void renderBackground(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.renderGuiBackground(guiGraphics, this.x(0), this.y(0), this.getXSize(), this.getYSize());

        this.name.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.x(10), this.y(8), Color.DARK_GRAY.getRGB(), false);

        guiGraphics.pose().pushPose();
        float scale = 0.9f;
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.drawString(this.font, SAVE_TO_CONFIG, (int) ((this.saveToConfig.getX() + 13) / scale), (int) ((this.saveToConfig.getY() + 2) / scale), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, IGNORE_AIR, (int) ((this.ignoreAir.getX() + 13) / scale), (int) ((this.ignoreAir.getY() + 2) / scale), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, SNBT, (int) ((this.nbtToSnbt.getX() + 13) / scale), (int) ((this.nbtToSnbt.getY() + 2) / scale), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, NETHER_VALIDATION, (int) ((this.netherValidation.getX() + 13) / scale), (int) ((this.netherValidation.getY() + 2) / scale), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.pose().popPose();

        if (this.saveToConfig.isHovered()) {
            guiGraphics.renderTooltip(this.font, SAVE_TO_CONFIG_DESC, mouseX, mouseY);
        } else if (this.ignoreAir.isHovered()) {
            guiGraphics.renderTooltip(this.font, IGNORE_AIR_DESC, mouseX, mouseY);
        } else if (this.nbtToSnbt.isHovered()) {
            guiGraphics.renderTooltip(this.font, SNBT_DESC, mouseX, mouseY);
        } else if (this.netherValidation.isHovered()) {
            guiGraphics.renderTooltip(this.font, NETHER_VALIDATION_DESC, mouseX, mouseY);
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
}
