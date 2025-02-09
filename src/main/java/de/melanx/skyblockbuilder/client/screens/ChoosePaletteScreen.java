package de.melanx.skyblockbuilder.client.screens;

import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ChoosePaletteScreen extends Screen {

    private static final int BUTTON_SIZE = 20;
    private static final int BUTTON_STEP = BUTTON_SIZE + 5;
    private final ConfiguredTemplate template;
    private final Consumer<Integer> applyIndex;
    private final Runnable resetIndex;
    private int paletteIndex = 0;
    private transient final Map<Integer, TemplateRenderer> structureCache = new HashMap<>();
    private int rows = 1;

    protected ChoosePaletteScreen(CustomizeSkyblockScreen parent, ConfiguredTemplate template) {
        super(Component.translatable("screen.skyblockbuilder.select_palette"));
        this.template = template;
        this.applyIndex = i -> parent.getTemplateList().getSelected().setPaletteIndex(i);
        this.resetIndex = () -> parent.getTemplateList().getSelected().resetPaletteIndex();
    }

    @Override
    protected void init() {
        super.init();
        assert this.minecraft != null;

        this.buildPaletteButtons();

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                    this.applyIndex.accept(this.paletteIndex);
                    this.minecraft.popGuiLayer();
                })
                .pos(this.width / 2 - 155, this.height - 28)
                .size(150, 20)
                .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
                    this.resetIndex.run();
                    this.minecraft.popGuiLayer();
                })
                .pos(this.width / 2 + 5, this.height - 28)
                .size(150, 20)
                .build());
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

        this.structureCache.forEach((i, renderer) -> renderer.setSize((float) (this.height - (this.height * 0.05) - ((this.rows - 1) * (BUTTON_STEP * 2)))));
    }

    @Override
    public void renderBackground(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int size = (int) (this.height - (this.height * 0.05) - ((this.rows - 1) * (BUTTON_STEP * 2)));

        this.structureCache.computeIfAbsent(this.paletteIndex, key -> new TemplateRenderer(this.template.getTemplate(), size, this.paletteIndex))
                .render(guiGraphics, this.width / 2, size / 2 + 10);
    }
}
