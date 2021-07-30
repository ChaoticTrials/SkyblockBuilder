package de.melanx.skyblockbuilder.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.skyblockbuilder.Registration;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ScreenCustomizeSkyblock extends Screen {

    private final Screen parent;
    private final Map<String, StructureTemplate> templateMap;
    private final Consumer<StructureTemplate> applyTemplate;
    private TemplateList list;
    private Button doneButton;
    private StructureTemplate template;

    public ScreenCustomizeSkyblock(Screen parent, StructureTemplate template) {
        super(Registration.customSkyblock.getDisplayName());
        this.parent = parent;
        this.template = template;
        TemplateLoader.updateTemplates();
        this.templateMap = TemplateLoader.getTemplates();
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
                .filter(entry -> Objects.equals(entry.template, this.template))
                .findFirst()
                .orElse(null));
    }

    private void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(0);
        this.list.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 8, Color.WHITE.getRGB());
        drawCenteredString(matrixStack, this.font, new TranslatableComponent("screen.skyblockbuilder.select_template"), this.width / 2, 28, Color.GRAY.getRGB());
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private class TemplateList extends ObjectSelectionList<TemplateList.TemplateEntry> {

        public TemplateList() {
            super(Objects.requireNonNull(ScreenCustomizeSkyblock.this.minecraft), ScreenCustomizeSkyblock.this.width, ScreenCustomizeSkyblock.this.height, 40, ScreenCustomizeSkyblock.this.height - 37, 16);
            ScreenCustomizeSkyblock.this.templateMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                this.addEntry(new TemplateEntry(entry.getKey(), entry.getValue()));
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
            private final StructureTemplate template;

            public TemplateEntry(String name, StructureTemplate template) {
                this.name = new TextComponent(name.replace(".nbt", ""));
                this.template = template;
            }

            @Override
            public void render(@Nonnull PoseStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
                GuiComponent.drawString(matrixStack, ScreenCustomizeSkyblock.this.font, this.name, left + 5, top + 2, Color.WHITE.getRGB());
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
