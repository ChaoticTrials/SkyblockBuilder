package de.melanx.skyblockbuilder.config;

import com.google.gson.JsonArray;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.noeppi_noeppi.libx.annotation.config.RegisterMapper;
import io.github.noeppi_noeppi.libx.config.ValidatorInfo;
import io.github.noeppi_noeppi.libx.config.ValueMapper;
import io.github.noeppi_noeppi.libx.config.gui.ConfigEditor;
import io.github.noeppi_noeppi.libx.config.gui.WidgetProperties;
import io.github.noeppi_noeppi.libx.impl.config.gui.EditorHelper;
import io.github.noeppi_noeppi.libx.impl.config.mappers.SimpleValueMappers;
import io.github.noeppi_noeppi.libx.screen.Panel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

@RegisterMapper
public class BlockPosMapper implements ValueMapper<BlockPos, JsonArray> {

    @Override
    public BlockPos fromJson(JsonArray json) {
        if (json.size() != 3) throw new IllegalStateException("Invalid BlockPos: " + json);
        return new BlockPos(json.get(0).getAsInt(), json.get(1).getAsInt(), json.get(2).getAsInt());
    }

    @Override
    public JsonArray toJson(BlockPos value) {
        JsonArray array = new JsonArray();
        array.add(value.getX());
        array.add(value.getY());
        array.add(value.getZ());
        return array;
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
                super(screen, properties.x(), properties.y(), properties.width(), properties.height());
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
            public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
                drawString(poseStack, this.font, "X", this.x, this.y + 6, Color.GRAY.getRGB());
                drawString(poseStack, this.font, "Y", this.x + 63, this.y + 6, Color.GRAY.getRGB());
                drawString(poseStack, this.font, "Z", this.x + 128, this.y + 6, Color.GRAY.getRGB());
                super.render(poseStack, mouseX, mouseY, partialTicks);
            }
        }
    }
}
