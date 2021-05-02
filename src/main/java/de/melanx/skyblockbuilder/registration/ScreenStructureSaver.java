package de.melanx.skyblockbuilder.registration;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public class ScreenStructureSaver extends Screen {

    private static final ResourceLocation SCREEN_LOCATION = new ResourceLocation(SkyblockBuilder.getInstance().modid, "textures/gui/structure_saver.png");

    private final int xSize;
    private final int ySize;
    private final ItemStack stack;
    @SuppressWarnings("FieldCanBeLocal")
    private int relX;
    @SuppressWarnings("FieldCanBeLocal")
    private int relY;
    private TextFieldWidget name;

    public ScreenStructureSaver(ItemStack stack, ITextComponent title) {
        super(title);
        this.xSize = 176;
        this.ySize = 85;
        this.stack = stack;
    }

    @Override
    public void init(@Nonnull Minecraft mc, int x, int y) {
        super.init(mc, x, y);
        this.relX = (x - this.xSize) / 2;
        this.relY = (y - this.ySize) / 2;
        this.name = new TextFieldWidget(this.font, this.relX + 15, this.relY + 25, 143, 17, new StringTextComponent("test"));
        this.name.setMaxStringLength(32767);
        this.name.changeFocus(true);
        this.name.setText(this.name.getText());
    }

    @Override
    public void render(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        //noinspection ConstantConditions
        this.minecraft.getTextureManager().bindTexture(SCREEN_LOCATION);
        this.blit(ms, this.relX, this.relY, 0, 0, this.xSize, this.ySize);

        for (Button button : Button.values()) {
            this.renderButton(ms, button, this.getHoveredButton(mouseX, mouseY) == button);
        }

        this.name.render(ms, mouseX, mouseY, partialTicks);
        super.render(ms, mouseX, mouseY, partialTicks);
        this.font.func_243248_b(ms, this.title, this.relX + 10, this.relY + 8, Color.DARK_GRAY.getRGB());
        for (Button button : Button.values()) {
            this.renderTitle(ms, button);
        }
    }

    public void renderButton(MatrixStack ms, Button button, boolean mouseHovered) {
        int xButton = this.relX + button.x;
        int yButton = this.relY + button.y;
        int width = button.width;
        int height = button.height;
        this.blit(ms, xButton, yButton, button.offset, mouseHovered ? this.ySize + 20 : this.ySize, width, height);
    }

    public void renderTitle(MatrixStack ms, Button button) {
        if (button.title == null) return;
        int xButton = this.relX + button.x;
        int yButton = this.relY + button.y;
        int stringLength = this.font.getStringWidth(button.title.getString());
        this.font.func_243246_a(ms, button.title, xButton + ((float) button.width / 2) - ((float) stringLength / 2), yButton + ((float) (button.height - 8) / 2), Color.WHITE.getRGB());
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0) {
            Button pressed = this.getHoveredButton((int) x, (int) y);
            if (pressed != null) {
                this.closeScreen();
                RandomUtility.playSound(SoundEvents.UI_BUTTON_CLICK);
                SkyblockBuilder.getNetwork().handleButtonClick(this.stack, pressed, this.name.getText().isEmpty() ? "template" : this.name.getText());
            }
        }
        return super.mouseClicked(x, y, button);
    }

    @Nullable
    @Override
    public IGuiEventListener getListener() {
        return this.name;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputMappings.Input mapping = InputMappings.getInputByCode(keyCode, scanCode);
        //noinspection ConstantConditions
        if (keyCode != 256 && (this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mapping)
                || this.minecraft.gameSettings.keyBindDrop.isActiveAndMatches(mapping))) {
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
        SAVE(10, 55, 60, 20, 0, new TranslationTextComponent("skyblockbuilder.screen.button.save")),
        ABORT(77, 55, 60, 20, 0, new TranslationTextComponent("skyblockbuilder.screen.button.abort")),
        DELETE(144, 55, 20, 20, 60, null);

        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final int offset;
        private final IFormattableTextComponent title;

        Button(int x, int y, int width, int height, int offset, IFormattableTextComponent title) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.offset = offset;
            this.title = title;
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
        public IFormattableTextComponent getTitle() {
            return this.title;
        }
    }
}
