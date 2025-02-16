package de.melanx.skyblockbuilder.client.screens;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.template.TemplateRenderer;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
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
        guiGraphics.drawCenteredString(this.font, Component.translatable("screen.skyblockbuilder.select_template"), this.width / 2, 28, Color.GRAY.getRGB());
        this.list.renderEntries(guiGraphics, mouseX, mouseY, partialTick);
    }

    class TemplateList extends ObjectSelectionList<TemplateList.TemplateEntry> {

        private transient final Map<String, TemplateRenderer> structureCache = new HashMap<>();

        public TemplateList() {
            super(Objects.requireNonNull(CustomizeSkyblockScreen.this.minecraft), CustomizeSkyblockScreen.this.width, CustomizeSkyblockScreen.this.height, 37, 40);
            AtomicInteger index = new AtomicInteger();
            CustomizeSkyblockScreen.this.templateMap.stream().sorted(Comparator.comparing(ConfiguredTemplate::getName)).forEach(entry -> {
                this.addEntry(new TemplateEntry(entry, index.getAndIncrement()));
            });
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
            if (this.getSelected() != null) {
                RenderSystem.enableBlend();
                int size = (this.width - this.getRowWidth()) / 2;
                boolean useIcon = this.getSelected().icon != null;

                if (useIcon) {
                    //noinspection ConstantConditions
                    int iconSize = this.getSelected().icon.getPixels().getHeight();
                    guiGraphics.blit(this.getSelected().iconLocation, 20, 85, size, size, 0, 0, iconSize, iconSize, iconSize, iconSize);
                } else {
                    String templateName = this.getSelected().name.getString();
                    this.structureCache.computeIfAbsent(templateName, key -> new TemplateRenderer(this.getSelected().template.getTemplate(), size))
                            .render(guiGraphics, (int) ((this.width - this.getRowWidth()) / 2f - (size / 2f)), this.getRowTop(0) + size / 2);
                }
                RenderSystem.disableBlend();
            }
        }

        @Override
        protected void renderListItems(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // delayed to #renderEntries to call it later
        }

        protected void renderEntries(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
        }

        class TemplateEntry extends ObjectSelectionList.Entry<TemplateEntry> {

            private final Component name;
            private final Component desc;
            private final ConfiguredTemplate template;
            private final boolean tooLong;
            private final ResourceLocation iconLocation;
            private final int index;
            private File iconFile;
            private final DynamicTexture icon;
            private Optional<Integer> paletteIndex = Optional.empty();

            public TemplateEntry(ConfiguredTemplate template, int index) {
                this.name = template.getNameComponent();
                this.desc = this.shortened(template.getDescriptionComponent());
                this.template = template;
                this.tooLong = !this.desc.getString().equals(template.getDescriptionComponent().getString());
                this.iconLocation = SkyblockBuilder.getInstance().resource(Util.sanitizeName(template.getName(), ResourceLocation::validPathChar) + "/icon");
                this.index = index;
                this.iconFile = SkyPaths.ICONS_DIR.resolve(template.getName().toLowerCase(Locale.ROOT) + ".png").toFile();
                if (!this.iconFile.isFile()) {
                    this.iconFile = null;
                }

                this.icon = this.loadIcon();
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
                        guiGraphics.renderTooltip(CustomizeSkyblockScreen.this.font, Component.translatable("screen.skyblockbuilder.select_palette"), mouseX, mouseY);
                    }
                }

                if (isMouseOver && this.tooLong) {
                    guiGraphics.renderTooltip(CustomizeSkyblockScreen.this.font, this.template.getDescriptionComponent(), mouseX, mouseY);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    if (this.canSelectPalette() && this.isMouseOverPaletteSelection(TemplateList.this.getRowLeft() + TemplateList.this.getRowWidth() - 28, TemplateList.this.getRowTop(this.index) + 1, mouseX, mouseY)) {
                        Minecraft.getInstance().pushGuiLayer(new ChoosePaletteScreen(CustomizeSkyblockScreen.this, this.template));
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
                TemplateList.this.structureCache.put(this.name.getString(), new TemplateRenderer(this.template.getTemplate(), (float) (TemplateList.this.width - TemplateList.this.getRowWidth()) / 2, index));
            }

            public Optional<Integer> getPaletteIndex() {
                return this.paletteIndex;
            }

            private DynamicTexture loadIcon() {
                if (this.iconFile != null && this.iconFile.isFile()) {
                    try {
                        FileInputStream in = new FileInputStream(this.iconFile);

                        DynamicTexture texture;
                        try {
                            NativeImage image = NativeImage.read(in);
                            Validate.validState(image.getWidth() == image.getHeight(), "Height and width must be equal.");
                            DynamicTexture tempTexture = new DynamicTexture(image);
                            Minecraft.getInstance().textureManager.register(this.iconLocation, tempTexture);
                            texture = tempTexture;
                        } catch (Throwable throwable) {
                            try {
                                in.close();
                            } catch (Throwable throwable1) {
                                throwable1.addSuppressed(throwable);
                            }

                            throw throwable;
                        }

                        in.close();
                        return texture;
                    } catch (Throwable throwable) {
                        SkyblockBuilder.getLogger().error("Invalid icon for template {}", this.template.getName(), throwable);
                        return null;
                    }
                } else {
                    Minecraft.getInstance().textureManager.release(this.iconLocation);
                    return null;
                }
            }

            private Component shortened(Component text) {
                String string = text.getString();
                String shorten = RandomUtility.shorten(Minecraft.getInstance().font, string, 210);
                return Component.literal(shorten);
            }
        }
    }
}
