package de.melanx.skyblockbuilder.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.template.TemplatePreview;
import de.melanx.skyblockbuilder.template.TemplatePreviewRenderer;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class CustomizeSkyblockScreen extends Screen {

    private static final ResourceLocation SELECT_PALETTE = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/page_forward.png");
    private final Screen parent;
    private final List<ConfiguredTemplate> templateMap;
    private final BiConsumer<ConfiguredTemplate, Optional<Integer>> applyTemplate;
    private TemplateList list;
    private Button doneButton;
    private ConfiguredTemplate template;

    public CustomizeSkyblockScreen(CreateWorldScreen parent, WorldCreationContext context) {
        super(Component.translatable("generator.skyblockbuilder.skyblock"));
        this.parent = parent;
        TemplateLoader.updateTemplates();
        this.template = TemplateLoader.getConfiguredTemplate();
        this.templateMap = TemplateLoader.getConfiguredTemplates();
        this.applyTemplate = TemplateLoader::setTemplate;
    }

    @Override
    protected void init() {
        Optional<Integer> paletteIndex = Optional.empty();
        if (this.list != null && this.list.getSelected() != null) {
            paletteIndex = this.list.getSelected().getPaletteIndex();
        }
        this.list = new TemplateList();
        this.addWidget(this.list);

        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                    this.applyTemplate.accept(this.template, this.list.getSelected() != null ? this.list.getSelected().getPaletteIndex() : Optional.empty());
                    //noinspection ConstantConditions
                    this.minecraft.setScreen(this.parent);
                })
                .pos(this.width / 2 - 155, this.height - 28)
                .size(150, 20)
                .build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
                    //noinspection ConstantConditions
                    this.minecraft.setScreen(this.parent);
                })
                .pos(this.width / 2 + 5, this.height - 28)
                .size(150, 20)
                .build());
        if (this.template != null) {
            this.list.setSelected(this.list.children().stream()
                    .filter(entry -> Objects.equals(entry.template.getTemplate(), this.template.getTemplate()))
                    .findFirst()
                    .orElse(null));
            if (this.list.getSelected() != null) {
                paletteIndex.ifPresent(this.list.getSelected()::setPaletteIndex);
            } else {
                this.list.setConfiguredStructureRenderer(new TemplatePreviewRenderer(new TemplatePreview(this.template), new TemplatePreviewRenderer.Area((this.width - this.list.getRowWidth()) / 2), 0));
            }
        }
    }

    @Nullable
    TemplateList getTemplateList() {
        return this.list;
    }

    private void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderMenuBackground(guiGraphics);
        this.list.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, Color.WHITE.getRGB());
        this.list.renderEntries(guiGraphics, mouseX, mouseY, partialTick);
    }

    class TemplateList extends ObjectSelectionList<TemplateList.TemplateEntry> {

        private transient final Map<String, TemplatePreviewRenderer> structureCache = new HashMap<>();
        private TemplatePreviewRenderer configuredStructureRenderer = null;

        public TemplateList() {
            super(Objects.requireNonNull(CustomizeSkyblockScreen.this.minecraft), CustomizeSkyblockScreen.this.width, CustomizeSkyblockScreen.this.height - 58, 25, 40);
            AtomicInteger index = new AtomicInteger();
            CustomizeSkyblockScreen.this.templateMap.stream().sorted(Comparator.comparing(ConfiguredTemplate::getName)).forEach(entry -> this.addEntry(
                    new TemplateEntry(entry, index.getAndIncrement())
            ));
        }

        @Override
        public boolean isFocused() {
            return CustomizeSkyblockScreen.this.getFocused() == this;
        }

        @Override
        public void setSelected(@Nullable TemplateEntry entry) {
            if (entry == this.getSelected()) return;
            super.setSelected(entry);

            if (entry != null) {
                CustomizeSkyblockScreen.this.template = entry.template;
            }

            CustomizeSkyblockScreen.this.updateButtonValidity();
        }

        @Override
        public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            boolean hasSelectedEntry = this.getSelected() != null;
            this.renderListBackground(guiGraphics);
            if (hasSelectedEntry || this.configuredStructureRenderer != null) {
                RenderSystem.enableBlend();
                TemplatePreviewRenderer renderer;
                if (hasSelectedEntry) {
                    String templateName = this.getSelected().name.getString();
                    renderer = this.structureCache.computeIfAbsent(templateName,
                            key -> new TemplatePreviewRenderer(new TemplatePreview(this.getSelected().template),
                                    new TemplatePreviewRenderer.Area(
                                            this.width / 100,
                                            this.getY() + 5,
                                            (this.width - this.getRowWidth()) / 2 - this.width / 100,
                                            this.height - 5
                                    )
                            )
                    );
                } else {
                    renderer = this.configuredStructureRenderer;
                }

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 1000);
                renderer.render(guiGraphics);
                guiGraphics.pose().popPose();
                RenderSystem.disableBlend();
            }
        }

        @Override
        protected void renderListItems(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // delayed to #renderEntries to call it later
        }

        protected void renderEntries(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.enableScissor(guiGraphics);
            super.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.disableScissor();
            this.renderListSeparators(guiGraphics);
        }

        public void setConfiguredStructureRenderer(@Nullable TemplatePreviewRenderer templateRenderer) {
            this.configuredStructureRenderer = templateRenderer;
        }

        class TemplateEntry extends ObjectSelectionList.Entry<TemplateEntry> {

            private final Component name;
            private final Component desc;
            private final ConfiguredTemplate template;
            private final boolean tooLong;
            private final int index;
            private Optional<Integer> paletteIndex = Optional.empty();

            public TemplateEntry(ConfiguredTemplate template, int index) {
                this.name = template.getNameComponent();
                this.desc = this.shortened(template.getDescriptionComponent());
                this.template = template;
                this.tooLong = !this.desc.getString().equals(template.getDescriptionComponent().getString());
                this.index = index;
            }

            @Override
            public void render(@Nonnull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                guiGraphics.drawString(CustomizeSkyblockScreen.this.font, this.name, left + 5, top + 7, Color.WHITE.getRGB());
                guiGraphics.drawString(CustomizeSkyblockScreen.this.font, this.desc, left + 5, top + 22, Color.GRAY.getRGB());

                if (this.canSelectPalette()) {
                    int textureX = left + width - 28;
                    int textureY = top + 1;

                    guiGraphics.blit(SELECT_PALETTE, textureX, textureY, 0, 0, 23, 13, 23, 13);

                    if (this.isMouseOverPaletteSelection(textureX, textureY, mouseX, mouseY)) {
                        guiGraphics.renderTooltip(CustomizeSkyblockScreen.this.font, SkyComponents.SCREEN_SELECT_PALETTE, mouseX, mouseY);
                    }
                }

                if (isMouseOver && this.tooLong) {
                    guiGraphics.renderTooltip(CustomizeSkyblockScreen.this.font, this.template.getDescriptionComponent(), mouseX, mouseY);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    if (this.canSelectPalette()
                            && this.isMouseOverPaletteSelection(
                            TemplateList.this.getRowLeft() + TemplateList.this.getRowWidth() - 28,
                            TemplateList.this.getRowTop(this.index) + 1,
                            mouseX, mouseY
                    )) {
                        Minecraft.getInstance().pushGuiLayer(
                                new ChoosePaletteScreen(this.template, this::setPaletteIndex, this::resetPaletteIndex)
                        );
                    }

                    TemplateList.this.setSelected(this);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                return super.isMouseOver(mouseX, mouseY);
            }

            private boolean canSelectPalette() {
                return this.template.getTemplate().palettes.size() > 1 && this.template.allowPaletteSelection();
            }

            private boolean isMouseOverPaletteSelection(int textureX, int textureY, double mouseX, double mouseY) {
                if (!this.canSelectPalette()) {
                    return false;
                }

                return mouseX >= textureX && mouseX <= textureX + 23 && mouseY >= textureY && mouseY <= textureY + 13;
            }

            @Nonnull
            @Override
            public Component getNarration() {
                return this.name;
            }

            public void resetPaletteIndex() {
                this.paletteIndex = Optional.empty();
                TemplateList.this.structureCache.remove(this.name.getString());
            }

            public void setPaletteIndex(int index) {
                this.paletteIndex = Optional.of(index);
                TemplateList.this.structureCache.put(this.name.getString(), new TemplatePreviewRenderer(new TemplatePreview(this.template), new TemplatePreviewRenderer.Area((TemplateList.this.width - TemplateList.this.getRowWidth()) / 2), index));
            }

            public Optional<Integer> getPaletteIndex() {
                return this.paletteIndex;
            }

            private Component shortened(Component text) {
                String string = text.getString();
                String shorten = RandomUtility.shorten(Minecraft.getInstance().font, string, 210);
                return Component.literal(shorten);
            }
        }
    }
}
