package de.melanx.skyblockbuilder.config.mapper;

import com.google.gson.JsonArray;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.gui.WidgetProperties;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.gui.EditorHelper;
import org.moddingx.libx.impl.config.mappers.SimpleValueMappers;
import org.moddingx.libx.screen.Panel;

import javax.annotation.Nullable;
import java.awt.Color;

@RegisterMapper
public class BlockPosMapper implements ValueMapper<BlockPos, JsonArray> {

    public static BlockPos fromJsonArray(JsonArray json) {
        if (json.size() != 3) throw new IllegalStateException("Invalid BlockPos: " + json);
        return new BlockPos(json.get(0).getAsInt(), json.get(1).getAsInt(), json.get(2).getAsInt());
    }

    public static JsonArray toJsonArray(BlockPos value) {
        JsonArray array = new JsonArray();
        array.add(value.getX());
        array.add(value.getY());
        array.add(value.getZ());
        return array;
    }

    @Override
    public BlockPos fromJson(JsonArray json) {
        return BlockPosMapper.fromJsonArray(json);
    }

    @Override
    public JsonArray toJson(BlockPos value) {
        return BlockPosMapper.toJsonArray(value);
    }

    @Override
    public Class<BlockPos> type() {
        return BlockPos.class;
    }

    @Override
    public Class<JsonArray> element() {
        return JsonArray.class;
    }

    @Override
    public ConfigEditor<BlockPos> createEditor(ValidatorInfo<?> validator) {
        return new BlockPosEditor();
    }

    private static class BlockPosEditor implements ConfigEditor<BlockPos> {

        @Override
        public BlockPos defaultValue() {
            return BlockPos.ZERO;
        }

        @Override
        public AbstractWidget createWidget(Screen screen, BlockPos initialValue, WidgetProperties<BlockPos> properties) {
            return new BlockPosEditor.BlockPosWidget(screen, initialValue, null, null, null, properties);
        }

        @Override
        public AbstractWidget updateWidget(Screen screen, AbstractWidget old, WidgetProperties<BlockPos> properties) {
            if (old instanceof BlockPosWidget) {
                return new BlockPosWidget(screen, ((BlockPosWidget) old).getPos(), ((BlockPosWidget) old).xWidget,
                        ((BlockPosWidget) old).yWidget, ((BlockPosWidget) old).zWidget, properties);
            } else {
                return this.createWidget(screen, this.defaultValue(), properties);
            }
        }

        private static class BlockPosWidget extends Panel {

            public final Font font;
            public final AbstractWidget xWidget;
            public final AbstractWidget yWidget;
            public final AbstractWidget zWidget;

            private int posX;
            private int posY;
            private int posZ;

            public BlockPosWidget(Screen screen, BlockPos value, @Nullable AbstractWidget xWidget, @Nullable AbstractWidget yWidget,
                                  @Nullable AbstractWidget zWidget, WidgetProperties<BlockPos> properties) {
                super(properties.x(), properties.y(), properties.width(), properties.height());
                this.font = Minecraft.getInstance().font;
                this.posX = value.getX();
                this.posY = value.getY();
                this.posZ = value.getZ();

                WidgetProperties<Integer> xProperties = new WidgetProperties<>(this.font.width("X") + 3, 0, 50, properties.height(), x -> {
                    this.posX = x;
                    properties.inputChanged().accept(new BlockPos(this.posX, this.posY, this.posZ));
                });

                WidgetProperties<Integer> yProperties = new WidgetProperties<>(this.font.width("XY") + 60, 0, 50, properties.height(), y -> {
                    this.posY = y;
                    properties.inputChanged().accept(new BlockPos(this.posX, this.posY, this.posZ));
                });

                WidgetProperties<Integer> zProperties = new WidgetProperties<>(this.font.width("XYZ") + 120, 0, 50, properties.height(), z -> {
                    this.posZ = z;
                    properties.inputChanged().accept(new BlockPos(this.posX, this.posY, this.posZ));
                });

                ConfigEditor<Integer> editor = SimpleValueMappers.INTEGER.createEditor(ValidatorInfo.empty());
                this.xWidget = this.addRenderableWidget(EditorHelper.create(screen, editor, this.posX, xWidget, xProperties));
                this.yWidget = this.addRenderableWidget(EditorHelper.create(screen, editor, this.posY, yWidget, yProperties));
                this.zWidget = this.addRenderableWidget(EditorHelper.create(screen, editor, this.posZ, zWidget, zProperties));
            }

            private BlockPos getPos() {
                return new BlockPos(this.posX, this.posY, this.posZ);
            }

            @Override
            protected void renderWidgetContent(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
                guiGraphics.drawString(this.font, "X", this.getX(), this.getY() + 6, Color.GRAY.getRGB());
                guiGraphics.drawString(this.font, "Y", this.getX() + 63, this.getY() + 6, Color.GRAY.getRGB());
                guiGraphics.drawString(this.font, "Z", this.getX() + 128, this.getY() + 6, Color.GRAY.getRGB());
            }
        }
    }
}
