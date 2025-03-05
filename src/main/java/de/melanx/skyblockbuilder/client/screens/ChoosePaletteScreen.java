package de.melanx.skyblockbuilder.client.screens;

import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateRenderer;
import de.melanx.skyblockbuilder.util.SkyComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ChoosePaletteScreen extends Screen {

    private static final int BUTTON_SIZE = 20;
    private static final int BUTTON_STEP = BUTTON_SIZE + 5;
    private final ConfiguredTemplate template;
    private final ChoosePaletteScreen.OnApply applyIndex;
    private final ChoosePaletteScreen.OnReset resetIndex;
    private int paletteIndex = 0;
    private transient final Map<Integer, TemplateRenderer> structureCache = new HashMap<>();
    private int rows = 1;
    private int structureRenderSize;

    protected ChoosePaletteScreen(ConfiguredTemplate template, ChoosePaletteScreen.OnApply onApply, ChoosePaletteScreen.OnReset onReset) {
        super(SkyComponents.SCREEN_SELECT_PALETTE);
        this.template = template;
        this.applyIndex = onApply;
        this.resetIndex = onReset;
    }

    @Override
    protected void init() {
        super.init();
        assert this.minecraft != null;

        this.buildPaletteButtons();

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                    this.applyIndex.onApply(this.paletteIndex);
                    this.minecraft.popGuiLayer();
                })
                .pos(this.width / 2 - 155, this.height - 28)
                .size(150, 20)
                .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
                    this.resetIndex.onReset();
                    this.minecraft.popGuiLayer();
                })
                .pos(this.width / 2 + 5, this.height - 28)
                .size(150, 20)
                .build());

        this.structureRenderSize = (int) (this.height - (this.height * 0.1) - ((this.rows - 1) * (BUTTON_STEP * 2)));
    }

    private void buildPaletteButtons() {
        int n = this.template.getTemplate().palettes.size();
        int rows = 1;
        while (true) {
            int buttonsInRow = (int) Math.ceil((double) n / rows);
            int rowTotalWidth = (buttonsInRow - 1) * BUTTON_STEP + BUTTON_SIZE;
            if (rowTotalWidth <= this.width) {
                break;
            }
            rows++;
        }

        this.rows = rows;
        int baseCount = n / rows;
        int extra = n % rows;

        int paletteIndex = 0;

        for (int i = 0; i < rows; i++) {
            int buttonsThisRow = (rows - i <= extra) ? (baseCount + 1) : baseCount;
            int rowTotalWidth = (buttonsThisRow - 1) * BUTTON_STEP + BUTTON_SIZE;
            int startX = (this.width - rowTotalWidth) / 2;
            int y = this.height - 60 - (rows - 1 - i) * BUTTON_STEP;
            for (int j = 0; j < buttonsThisRow; j++) {
                final int currentPaletteIndex = paletteIndex;
                this.addRenderableWidget(Button.builder(Component.literal(String.valueOf(currentPaletteIndex + 1)),
                                button -> this.updatePalette(currentPaletteIndex))
                        .pos(startX + (j * BUTTON_STEP), y)
                        .size(BUTTON_SIZE, BUTTON_SIZE)
                        .build()
                );
                paletteIndex++;
            }
        }
    }

    private void updatePalette(int paletteIndex) {
        this.paletteIndex = paletteIndex;
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);

        this.structureCache.forEach((i, renderer) -> renderer.setSize(this.structureRenderSize));
    }

    @Override
    public void renderBackground(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.structureCache.computeIfAbsent(this.paletteIndex, key -> new TemplateRenderer(this.template.getTemplate(), this.structureRenderSize, this.paletteIndex))
                .render(guiGraphics, this.width / 2, this.structureRenderSize / 2 + (int) (this.height * 0.05));
    }

    @FunctionalInterface
    public interface OnReset {
        void onReset();
    }

    @FunctionalInterface
    public interface OnApply {
        void onApply(int paletteIndex);
    }
}
