package de.melanx.skyblockbuilder.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyblockbuilder.Registration;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ScreenCustomizeSkyblock extends Screen {

    private final Screen parent;
    private final List<ConfiguredTemplate> templateMap;
    private final Consumer<ConfiguredTemplate> applyTemplate;
    private TemplateList list;
    private Button doneButton;
    private ConfiguredTemplate template;

    public ScreenCustomizeSkyblock(Screen parent, ConfiguredTemplate template) {
        super(Registration.customSkyblock.getDisplayName());
        this.parent = parent;
        this.template = template;
        TemplateLoader.updateTemplates();
        this.templateMap = TemplateLoader.getConfiguredTemplates();
        this.applyTemplate = TemplateLoader::setTemplate;
    }

    @Override
    protected void init() {
        //noinspection ConstantConditions
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.list = new TemplateList();
        this.addWidget(this.list);

        this.doneButton = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 28, 150, 20, CommonComponents.GUI_DONE, (p_241579_1_) -> {
            this.applyTemplate.accept(this.template);
            this.minecraft.setScreen(this.parent);
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, (p_213015_1_) -> {
            this.minecraft.setScreen(this.parent);
        }));
        this.list.setSelected(this.list.children().stream()
                .filter(entry -> Objects.equals(entry.template.getTemplate(), this.template.getTemplate()))
                .findFirst()
                .orElse(null));
    }

    private void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(0);
        this.list.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, Color.WHITE.getRGB());
        drawCenteredString(poseStack, this.font, new TranslatableComponent("screen.skyblockbuilder.select_template"), this.width / 2, 28, Color.GRAY.getRGB());
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private class TemplateList extends ObjectSelectionList<TemplateList.TemplateEntry> {

        public TemplateList() {
            super(Objects.requireNonNull(ScreenCustomizeSkyblock.this.minecraft), ScreenCustomizeSkyblock.this.width, ScreenCustomizeSkyblock.this.height, 40, ScreenCustomizeSkyblock.this.height - 37, 16);
            ScreenCustomizeSkyblock.this.templateMap.stream().sorted(Comparator.comparing(ConfiguredTemplate::getName)).forEach(entry -> {
                this.addEntry(new TemplateEntry(entry));
            });
        }

        @Override
        protected boolean isFocused() {
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

        private class TemplateEntry extends ObjectSelectionList.Entry<TemplateEntry> {

            private final Component name;
            private final ConfiguredTemplate template;

            public TemplateEntry(ConfiguredTemplate template) {
                this.name = new TextComponent(template.getName());
                this.template = template;
            }

            @Override
            public void render(@Nonnull PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
                GuiComponent.drawString(poseStack, ScreenCustomizeSkyblock.this.font, this.name, left + 5, top + 2, Color.WHITE.getRGB());
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
        }
    }
}
