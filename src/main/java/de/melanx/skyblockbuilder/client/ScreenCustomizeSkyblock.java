package de.melanx.skyblockbuilder.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.melanx.skyblockbuilder.Registration;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.feature.template.Template;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ScreenCustomizeSkyblock extends Screen {

    private final Screen parent;
    private final Map<String, Template> templateMap;
    private final Consumer<Template> applyTemplate;
    private TemplateList list;
    private Button doneButton;
    private Template template;

    public ScreenCustomizeSkyblock(Screen parent, Template template) {
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
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.list = new TemplateList();
        this.children.add(this.list);

        this.doneButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, DialogTexts.GUI_DONE, (p_241579_1_) -> {
            this.applyTemplate.accept(this.template);
            this.minecraft.displayGuiScreen(this.parent);
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, DialogTexts.GUI_CANCEL, (p_213015_1_) -> {
            this.minecraft.displayGuiScreen(this.parent);
        }));
        this.list.setSelected(this.list.getEventListeners().stream()
                .filter(entry -> Objects.equals(entry.template, this.template))
                .findFirst()
                .orElse(null));
    }

    private void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(0);
        this.list.render(ms, mouseX, mouseY, partialTicks);
        drawCenteredString(ms, this.font, this.title, this.width / 2, 8, Color.WHITE.getRGB());
        drawCenteredString(ms, this.font, new TranslationTextComponent("screen.skyblockbuilder.select_template"), this.width / 2, 28, Color.GRAY.getRGB());
        super.render(ms, mouseX, mouseY, partialTicks);
    }

    private class TemplateList extends ExtendedList<TemplateList.TemplateEntry> {

        public TemplateList() {
            super(Objects.requireNonNull(ScreenCustomizeSkyblock.this.minecraft), ScreenCustomizeSkyblock.this.width, ScreenCustomizeSkyblock.this.height, 40, ScreenCustomizeSkyblock.this.height - 37, 16);
            ScreenCustomizeSkyblock.this.templateMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                this.addEntry(new TemplateEntry(entry.getKey(), entry.getValue()));
            });
        }

        @Override
        protected boolean isFocused() {
            return ScreenCustomizeSkyblock.this.getListener() == this;
        }

        @Override
        public void setSelected(@Nullable TemplateEntry entry) {
            super.setSelected(entry);

            if (entry != null) {
                ScreenCustomizeSkyblock.this.template = entry.template;
            }

            ScreenCustomizeSkyblock.this.updateButtonValidity();
        }

        private class TemplateEntry extends AbstractList.AbstractListEntry<TemplateEntry> {

            private final ITextComponent name;
            private final Template template;

            public TemplateEntry(String name, Template template) {
                this.name = new StringTextComponent(name.replace(".nbt", ""));
                this.template = template;
            }

            @Override
            public void render(@Nonnull MatrixStack ms, int p_230432_2_, int y, int x, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float partialTicks) {
                AbstractGui.drawString(ms, ScreenCustomizeSkyblock.this.font, this.name, x + 5, y + 2, Color.WHITE.getRGB());
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
        }
    }
}
