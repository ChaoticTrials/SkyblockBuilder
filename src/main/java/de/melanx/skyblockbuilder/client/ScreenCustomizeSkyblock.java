package de.melanx.skyblockbuilder.client;

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
import java.util.function.Consumer;

public class ScreenCustomizeSkyblock extends Screen {

    private final Screen parent;
    private final List<ConfiguredTemplate> templateMap;
    private final Consumer<ConfiguredTemplate> applyTemplate;
    private TemplateList list;
    private Button doneButton;
    private ConfiguredTemplate template;

    public ScreenCustomizeSkyblock(CreateWorldScreen parent, WorldCreationContext context) {
        super(Component.translatable("generator.skyblockbuilder.skyblock"));
        this.parent = parent;
        TemplateLoader.updateTemplates();
        this.template = TemplateLoader.getConfiguredTemplate();
        this.templateMap = TemplateLoader.getConfiguredTemplates();
        this.applyTemplate = TemplateLoader::setTemplate;
    }

    @Override
    protected void init() {
        this.list = new TemplateList();
        this.addWidget(this.list);

        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                    this.applyTemplate.accept(this.template);
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
        }
    }

    private void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderDirtBackground(guiGraphics);
        this.list.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, Color.WHITE.getRGB());
        guiGraphics.drawCenteredString(this.font, Component.translatable("screen.skyblockbuilder.select_template"), this.width / 2, 28, Color.GRAY.getRGB());
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.list.renderEntries(guiGraphics, mouseX, mouseY, partialTick);
    }

    private class TemplateList extends ObjectSelectionList<TemplateList.TemplateEntry> {

        private final Set<File> loggedLocations = new HashSet<>();
        private transient final Map<String, TemplateRenderer> structureCache = new HashMap<>();

        public TemplateList() {
            super(Objects.requireNonNull(ScreenCustomizeSkyblock.this.minecraft), ScreenCustomizeSkyblock.this.width, ScreenCustomizeSkyblock.this.height, 40, ScreenCustomizeSkyblock.this.height - 37, 40);
            ScreenCustomizeSkyblock.this.templateMap.stream().sorted(Comparator.comparing(ConfiguredTemplate::getName)).forEach(entry -> {
                this.addEntry(new TemplateEntry(entry));
            });
        }

        @Override
        public boolean isFocused() {
            return ScreenCustomizeSkyblock.this.getFocused() == this;
        }

        @Override
        public void setSelected(@Nullable TemplateEntry entry) {
            super.setSelected(entry);

            if (entry != null) {
                ScreenCustomizeSkyblock.this.template = entry.template;
            }

            ScreenCustomizeSkyblock.this.updateButtonValidity();
        }

        @Override
        public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);

            if (this.getSelected() != null) {
                RenderSystem.enableBlend();
                int size = (this.width - 220) / 2 - 40;
                boolean useIcon = this.getSelected().icon != null;

                if (useIcon) {
                    //noinspection ConstantConditions
                    int iconSize = this.getSelected().icon.getPixels().getHeight();
                    guiGraphics.blit(this.getSelected().iconLocation, 20, 85, size, size, 0, 0, iconSize, iconSize, iconSize, iconSize);
                } else {
                    String templateName = this.getSelected().name.getString();
                    if (!this.structureCache.containsKey(templateName)) {
                        this.structureCache.put(templateName, new TemplateRenderer(this.getSelected().template.getTemplate(), size));
                    }
                    this.structureCache.get(templateName).render(guiGraphics, 103, 182);
                }
                RenderSystem.disableBlend();
            }
        }

        @Override
        protected void renderList(@Nonnull GuiGraphics guiGraphics, int x, int y, float partialTick) {
            // delayed to #renderEntries to call it later
        }

        protected void renderEntries(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderList(guiGraphics, mouseX, mouseY, partialTick);
        }

        private class TemplateEntry extends ObjectSelectionList.Entry<TemplateEntry> {

            private final Component name;
            private final Component desc;
            private final ConfiguredTemplate template;
            private final boolean tooLong;
            private final ResourceLocation iconLocation;
            private File iconFile;
            private final DynamicTexture icon;

            public TemplateEntry(ConfiguredTemplate template) {
                this.name = template.getNameComponent();
                this.desc = this.shortened(template.getDescriptionComponent());
                this.template = template;
                this.tooLong = !this.desc.getString().equals(template.getDescriptionComponent().getString());
                this.iconLocation = SkyblockBuilder.getInstance().resource(Util.sanitizeName(template.getName(), ResourceLocation::validPathChar) + "/icon");
                this.iconFile = SkyPaths.ICONS_DIR.resolve(template.getName().toLowerCase(Locale.ROOT) + ".png").toFile();
                if (!this.iconFile.isFile()) {
                    if (!TemplateList.this.loggedLocations.contains(this.iconFile)) {
                        TemplateList.this.loggedLocations.add(this.iconFile);
                        SkyblockBuilder.getLogger().info("No icon set for template '{}'. Should be at this location: '{}'", template.getName(), this.iconFile);
                    }
                    this.iconFile = null;
                }

                this.icon = this.loadIcon();
            }

            @Override
            public void render(@Nonnull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                guiGraphics.drawString(ScreenCustomizeSkyblock.this.font, this.name, left + 5, top + 7, Color.WHITE.getRGB());
                guiGraphics.drawString(ScreenCustomizeSkyblock.this.font, this.desc, left + 5, top + 22, Color.GRAY.getRGB());
                if (isMouseOver && this.tooLong) {
                    guiGraphics.renderTooltip(ScreenCustomizeSkyblock.this.font, this.template.getDescriptionComponent(), mouseX, mouseY);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    TemplateList.this.setSelected(this);
                    return true;
                } else {
                    return false;
                }
            }

            @Nonnull
            @Override
            public Component getNarration() {
                return this.name;
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
