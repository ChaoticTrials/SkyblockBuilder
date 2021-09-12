package de.melanx.skyblockbuilder.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.ClientUtility;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public class ScreenStructureSaver extends Screen {

    private static final ResourceLocation SCREEN_LOCATION = new ResourceLocation(SkyblockBuilder.getInstance().modid, "textures/gui/structure_saver.png");

    private final int xSize;
    private final int ySize;
    private final ItemStack stack;
    private int relX;
    private int relY;
    private EditBox name;

    public ScreenStructureSaver(ItemStack stack, Component title) {
        super(title);
        this.xSize = 176;
        this.ySize = 85;
        this.stack = stack;
    }

    @Override
    protected void init() {
        this.relX = (this.width - this.xSize) / 2;
        this.relY = (this.height - this.ySize) / 2;
        this.name = new EditBox(this.font, this.relX + 11, this.relY + 25, 125, 17, new TranslatableComponent("skyblockbuilder.screen.widget.structure_name"));
        this.name.setMaxLength(32767);
        this.name.changeFocus(true);
        this.name.setValue(this.name.getValue());
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, SCREEN_LOCATION);
        this.blit(poseStack, this.relX, this.relY, 0, 0, this.xSize, this.ySize);

        Button hoveredButton = this.getHoveredButton(mouseX, mouseY);
        for (Button button : Button.values()) {
            this.renderButton(poseStack, button, hoveredButton == button);
        }

        this.name.render(poseStack, mouseX, mouseY, partialTicks);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.font.draw(poseStack, this.title, this.relX + 10, this.relY + 8, Color.DARK_GRAY.getRGB());
        for (Button button : Button.values()) {
            this.renderTitle(poseStack, button);
            if (hoveredButton == button && button.tooltip != null) {
                this.renderTooltip(poseStack, button.tooltip, mouseX, mouseY);
            }
        }
    }

    public void renderButton(PoseStack ms, Button button, boolean mouseHovered) {
        int xButton = this.relX + button.x;
        int yButton = this.relY + button.y;
        int width = button.width;
        int height = button.height;
        this.blit(ms, xButton, yButton, button.offset, mouseHovered ? this.ySize + 20 : this.ySize, width, height);
    }

    public void renderTitle(PoseStack ms, Button button) {
        if (button.title == null) return;
        int xButton = this.relX + button.x;
        int yButton = this.relY + button.y;
        int stringLength = this.font.width(button.title.getString());
        this.font.drawShadow(ms, button.title, xButton + ((float) button.width / 2) - ((float) stringLength / 2), yButton + ((float) (button.height - 8) / 2), Color.WHITE.getRGB());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Button pressed = this.getHoveredButton((int) mouseX, (int) mouseY);
            if (pressed != null) {
                if (pressed == Button.OPEN_FOLDER) {
                    ClientUtility.openPath("skyblock_exports");
                } else {
                    this.onClose();
                }

                ClientUtility.playSound(SoundEvents.UI_BUTTON_CLICK);
                SkyblockBuilder.getNetwork().handleButtonClick(this.stack, pressed, this.name.getValue().isEmpty() ? "template" : this.name.getValue());
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
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

    @Nullable
    public Button getHoveredButton(int x, int y) {
        for (Button button : Button.values()) {
            int xButton = this.relX + button.x;
            int yButton = this.relY + button.y;
            if (x >= xButton && x < xButton + button.width && y >= yButton && y < yButton + button.height) {
                return button;
            }
        }

        return null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public enum Button {
        SAVE(10, 55, 60, 20, 0, new TranslatableComponent("skyblockbuilder.screen.button.save"), null),
        ABORT(77, 55, 60, 20, 0, new TranslatableComponent("skyblockbuilder.screen.button.abort"), null),
        DELETE(144, 55, 20, 20, 60, null, new TranslatableComponent("skyblockbuilder.screen.button.delete.tooltip")),
        OPEN_FOLDER(144, 23, 20, 20, 80, null, new TranslatableComponent("skyblockbuilder.screen.button.open_folder.tooltip"));

        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final int offset;
        private final MutableComponent title;
        private final MutableComponent tooltip;

        Button(int x, int y, int width, int height, int offset, MutableComponent title, MutableComponent tooltip) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.offset = offset;
            this.title = title;
            this.tooltip = tooltip;
        }

        /**
         * @return x coordinate in screen
         */
        public int getX() {
            return this.x;
        }

        /**
         * @return y coordinate in screen
         */
        public int getY() {
            return this.y;
        }

        /**
         * @return button width
         */
        public int getWidth() {
            return this.width;
        }

        /**
         * @return button height
         */
        public int getHeight() {
            return this.height;
        }

        /**
         * @return x offset in texture
         */
        public int getOffset() {
            return this.offset;
        }

        /**
         * @return showed title on button
         */
        @Nullable
        public MutableComponent getTitle() {
            return this.title;
        }

        /**
         * @return tooltip when hovered over button
         */
        @Nullable
        public MutableComponent getTooltip() {
            return this.tooltip;
        }
    }
}
