package de.melanx.skyblockbuilder.client.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.ClientUtil;
import de.melanx.skyblockbuilder.client.SizeableCheckbox;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.item.StructureSaverSettings;
import de.melanx.skyblockbuilder.registration.ModBlocks;
import de.melanx.skyblockbuilder.registration.ModDataComponentTypes;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class StructureSaverScreen extends BaseScreen {

    private static final ResourceLocation[] UNSELECTED_TOP_TABS = new ResourceLocation[]{
            ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_1"),
            ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_2"),
            ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_3")
    };
    private static final ResourceLocation[] SELECTED_TOP_TABS = new ResourceLocation[]{
            ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_1"),
            ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_2"),
            ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_3")
    };

    private static final ResourceLocation SCREEN_LOCATION = ResourceLocation.fromNamespaceAndPath(SkyblockBuilder.getInstance().modid, "textures/gui/structure_saver.png");
    private static final Component SAVE_TO_CONFIG = SkyComponents.ITEM_STRUCTURE_SAVER_SAVE_TO_CONFIG_TOOLTIP;
    private static final Component IGNORE_AIR = SkyComponents.ITEM_STRUCTURE_SAVER_IGNORE_AIR_TOOLTIP;
    private static final Component SNBT = SkyComponents.ITEM_STRUCTURE_SAVER_NBT_TO_SNBT_TOOLTIP;
    private static final Component KEEP_POSITIONS = SkyComponents.ITEM_STRUCTURE_SAVER_KEEP_POSITIONS_TOOLTIP;
    private static final Component SAVE_TO_CONFIG_DESC = SkyComponents.ITEM_STRUCTURE_SAVER_SAVE_TO_CONFIG_DESC;
    private static final Component IGNORE_AIR_DESC = SkyComponents.ITEM_STRUCTURE_SAVER_IGNORE_AIR_DESC;
    private static final Component SNBT_DESC = SkyComponents.ITEM_STRUCTURE_SAVER_NBT_TO_SNBT_DESC;
    private static final Component KEEP_POSITIONS_DESC = SkyComponents.ITEM_STRUCTURE_SAVER_KEEP_POSITIONS_DESC;

    private final ItemStack stack;
    private EditBox name;
    private Checkbox ignoreAir;
    private Checkbox nbtToSnbt;
    private Checkbox saveToConfig;
    private Checkbox keepPositions;
    private StructureSaverSettings.Type selectedType;
    private Supplier<Boolean> validator;
    private StructureSaverSettings currentSettings;

    public StructureSaverScreen(ItemStack stack) {
        super(174, 142, SkyComponents.SCREEN_STRUCTURE_SAVER);
        this.stack = stack;
        this.currentSettings = stack.getOrDefault(ModDataComponentTypes.structureSaverSettings, StructureSaverSettings.DEFAULT);
    }

    @Override
    protected void init() {
        super.init();
        this.selectedType = this.stack.getOrDefault(ModDataComponentTypes.structureSaverType, StructureSaverSettings.Type.ISLAND);

        this.validator = () -> true;
        this.initForType(this.selectedType);
    }

    private void initForType(StructureSaverSettings.Type type) {
        this.addRenderableWidget(Button.builder(SkyComponents.SCREEN_BUTTON_SAVE, button -> {
                    if (!this.validator.get()) {
                        Objects.requireNonNull(this.minecraft).setScreen(new ErrorScreen(type));
                        return;
                    }

                    SkyblockBuilder.getNetwork().saveStructure(this.stack, this.currentSettings);
                    this.onClose();
                })
                .pos(this.x(10), this.y(50))
                .size(60, 20)
                .build());
        this.addRenderableWidget(Button.builder(SkyComponents.SCREEN_BUTTON_DELETE, button -> {
                    SkyblockBuilder.getNetwork().deleteTags();
                    this.onClose();
                })
                .pos(this.x(77), this.y(50))
                .size(60, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.empty(), button -> {
                    if (this.saveToConfig.selected()) {
                        switch (this.selectedType) {
                            case ISLAND -> ClientUtil.openPath(SkyPaths.ISLANDS_DIR);
                            case SPREAD -> ClientUtil.openPath(SkyPaths.SPREADS_DIR);
                            case NETHER -> ClientUtil.openPath(SkyPaths.PORTALS_DIR);
                        }
                    } else {
                        ClientUtil.openPath(SkyPaths.MOD_EXPORTS);
                    }
                })
                .pos(this.x(144), this.y(23))
                .size(20, 20)
                .tooltip(Tooltip.create(SkyComponents.SCREEN_BUTTON_OPEN_FOLDER_TOOLTIP))
                .build());

        AtomicInteger heightIndex = new AtomicInteger();
        int heightMargin = 15;
        int baseHeight = 78;
        Supplier<Integer> height = () -> baseHeight + heightMargin * heightIndex.getAndIncrement();
        this.name = new EditBox(this.font, this.x(11), this.y(25), 125, 17, SkyComponents.SCREEN_WIDGET_STRUCTURE_NAME);
        this.name.setMaxLength(Short.MAX_VALUE);
        this.saveToConfig = this.addRenderableWidget(new SizeableCheckbox(this.x(10), this.y(height.get()), 10, this.currentSettings.saveToConfig()));
        this.ignoreAir = this.addRenderableWidget(new SizeableCheckbox(this.x(10), this.y(height.get()), 10, this.currentSettings.ignoreAir()));
        this.nbtToSnbt = this.addRenderableWidget(new SizeableCheckbox(this.x(10), this.y(height.get()), 10, this.currentSettings.nbtToSnbt()));
        this.keepPositions = this.addRenderableWidget(new SizeableCheckbox(this.x(10), this.y(height.get()), 10, this.currentSettings.keepPositions()));

        switch (type) {
            case ISLAND, SPREAD -> {
                this.name.setFocused(true);
                this.name.setValue(this.currentSettings.name());
                if (type == StructureSaverSettings.Type.ISLAND) {
                    this.validator = () -> {
                        BoundingBox boundingBox = ItemStructureSaver.getArea(this.stack);
                        if (boundingBox == null) {
                            return false;
                        }

                        return Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockStates(AABB.of(boundingBox)).anyMatch(blockState -> blockState.is(ModBlocks.spawnBlock));
                    };
                }
            }
            case NETHER -> {
                this.name.setValue("to_nether");
                this.validator = () -> {
                    BoundingBox boundingBox = ItemStructureSaver.getArea(this.stack);
                    if (boundingBox == null) {
                        return false;
                    }

                    return Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockStates(AABB.of(boundingBox)).anyMatch(blockState -> blockState.is(Blocks.NETHER_PORTAL));
                };
            }
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.blit(SCREEN_LOCATION, this.x(146), this.y(25), 0, 0, 16, 16, 16, 16);

        StructureSaverSettings.Type hoveredType = this.getHoveredTypeTab(mouseX, mouseY);
        if (hoveredType != null) {
            this.setTooltipForNextRenderPass(hoveredType.getTooltip());
        }
    }

    @Override
    public void renderBackground(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        for (int i = 0; i < 3; i++) {
            if (i == this.selectedType.ordinal()) {
                continue;
            }

            guiGraphics.blitSprite(UNSELECTED_TOP_TABS[i], this.x(i * 27), this.y(-28), 26, 32);
            guiGraphics.renderItem(StructureSaverSettings.Type.values()[i].getStack(), this.x((i * 27) + 5), this.y(-19));
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.renderGuiBackground(guiGraphics, this.x(0), this.y(0), this.getXSize(), this.getYSize());

        this.name.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.x(10), this.y(8), Color.DARK_GRAY.getRGB(), false);

        guiGraphics.pose().pushPose();
        float scale = 0.9f;
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.drawString(this.font, SAVE_TO_CONFIG, (int) ((this.saveToConfig.getX() + 13) / scale), (int) ((this.saveToConfig.getY() + 2) / scale), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, IGNORE_AIR, (int) ((this.ignoreAir.getX() + 13) / scale), (int) ((this.ignoreAir.getY() + 2) / scale), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, SNBT, (int) ((this.nbtToSnbt.getX() + 13) / scale), (int) ((this.nbtToSnbt.getY() + 2) / scale), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, KEEP_POSITIONS, (int) ((this.keepPositions.getX() + 13) / scale), (int) ((this.keepPositions.getY() + 2) / scale), Color.DARK_GRAY.getRGB(), false);
        guiGraphics.pose().popPose();

        if (this.saveToConfig.isHovered()) {
            guiGraphics.renderTooltip(this.font, SAVE_TO_CONFIG_DESC, mouseX, mouseY);
        } else if (this.ignoreAir.isHovered()) {
            guiGraphics.renderTooltip(this.font, IGNORE_AIR_DESC, mouseX, mouseY);
        } else if (this.nbtToSnbt.isHovered()) {
            guiGraphics.renderTooltip(this.font, SNBT_DESC, mouseX, mouseY);
        } else if (this.keepPositions.isHovered()) {
            guiGraphics.renderTooltip(this.font, KEEP_POSITIONS_DESC, mouseX, mouseY);
        }

        guiGraphics.blitSprite(SELECTED_TOP_TABS[this.selectedType.ordinal()], this.x(this.selectedType.ordinal() * 27), this.y(-28), 26, 32);
        guiGraphics.renderItem(this.selectedType.getStack(), this.x((this.selectedType.ordinal() * 27) + 5), this.y(-19));
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.name;
    }

    @Nullable
    public StructureSaverSettings.Type getHoveredTypeTab(int x, int y) {
        for (int i = 0; i < StructureSaverSettings.Type.values().length; i++) {
            int tabX = this.x(i * 27);
            int tabY = this.y(-28);
            if (x >= tabX && x < tabX + 26 && y >= tabY && y < tabY + 32) {
                return StructureSaverSettings.Type.values()[i];
            }
        }

        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.currentSettings = new StructureSaverSettings(this.name.getValue(), this.saveToConfig.selected(), this.ignoreAir.selected(), this.nbtToSnbt.selected(), this.keepPositions.selected());
        StructureSaverSettings.Type hoveredType = this.getHoveredTypeTab((int) mouseX, (int) mouseY);
        if (hoveredType != null) {
            SkyblockBuilder.getNetwork().changeStructureSaverType(this.stack, hoveredType);
            this.stack.set(ModDataComponentTypes.structureSaverType, hoveredType);
            this.selectedType = hoveredType;
            this.rebuildWidgets();

            return true;
        }

        boolean result = super.mouseClicked(mouseX, mouseY, button);
        if (result) {
            SkyblockBuilder.getNetwork().updateStructureSaverSettings(this.stack, this.currentSettings);
        }

        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mapping = InputConstants.getKey(keyCode, scanCode);
        //noinspection ConstantConditions
        if (keyCode != InputConstants.KEY_ESCAPE && (this.minecraft.options.keyInventory.isActiveAndMatches(mapping)
                || this.minecraft.options.keyDrop.isActiveAndMatches(mapping))) {
            return true;
        }

        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        if (result) {
            this.currentSettings = new StructureSaverSettings(this.name.getValue(), this.saveToConfig.selected(), this.ignoreAir.selected(), this.nbtToSnbt.selected(), this.keepPositions.selected());
            SkyblockBuilder.getNetwork().updateStructureSaverSettings(this.stack, this.currentSettings);
        }

        return result;
    }

    private static class ErrorScreen extends BaseScreen {

        private final StructureSaverSettings.Type type;
        private final boolean missingBlockHasItem;

        protected ErrorScreen(StructureSaverSettings.Type type) {
            super(Minecraft.getInstance().font.width(SkyComponents.SCREEN_ERROR_MESSAGE) + 20, 100, SkyComponents.SCREEN_ERROR_TITLE);
            this.type = type;
            this.missingBlockHasItem = type.getRequiredBlock().asItem() != Items.AIR;
        }

        @Override
        protected void init() {
            super.init();

            boolean mayNotCheat = this.minecraft == null
                    || this.minecraft.player == null
                    || !(this.minecraft.player.hasPermissions(2)
                    || this.minecraft.player.isCreative());

            Button.Builder buttonBuilder;
            if (this.missingBlockHasItem && !mayNotCheat) {
                buttonBuilder = Button.builder(SkyComponents.SCREEN_ERROR_GIVE_BUTTON, button -> {
                    SkyblockBuilder.getNetwork().giveItem(this.type.getRequiredBlock().asItem());
                    this.onClose();
                });
            } else {
                buttonBuilder = Button.builder(CommonComponents.GUI_OK, button -> {
                    this.onClose();
                });
            }

            this.addRenderableWidget(buttonBuilder
                    .pos(this.centeredX(100), this.y(65))
                    .size(100, 20)
                    .build());
        }

        @Override
        public void renderBackground(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.renderGuiBackground(guiGraphics, this.x(0), this.y(0), this.getXSize(), this.getYSize());

            guiGraphics.drawString(this.font, this.title, this.centeredX(this.font.width(this.title)), this.y(8), Color.RED.getRGB(), false);
            guiGraphics.drawString(this.font, SkyComponents.SCREEN_ERROR_MESSAGE, this.x(10), this.y(25), Color.DARK_GRAY.getRGB(), false);

            int blockX = this.x(15);
            if (this.missingBlockHasItem) {
                guiGraphics.renderItem(new ItemStack(this.type.getRequiredBlock()), this.x(10), this.y(38));
                blockX += 15;
            }

            guiGraphics.drawString(this.font, this.type.getRequiredBlock().getName(), blockX, this.y(43), Color.DARK_GRAY.getRGB(), false);
        }
    }
}
