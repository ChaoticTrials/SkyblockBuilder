package de.melanx.skyblockbuilder.template;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.FakeLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.moddingx.libx.render.ClientTickHandler;

import java.util.*;

/*
 * Most code taken from Patchouli by Vazkii
 * https://github.com/VazkiiMods/Patchouli/blob/35ae32b6b9c9c37a78ecd4867b83ba25304fd0c7/Common/src/main/java/vazkii/patchouli/client/book/page/PageMultiblock.java
 * The license of Patchouli applies to this I guess.
 */
public class TemplateRenderer {

    private final ClientLevel clientLevel = Objects.requireNonNull(FakeLevel.getInstance());
    private final StructureTemplate template;
    private final transient Map<BlockPos, BlockEntity> teCache = new HashMap<>();
    private final transient Set<BlockEntity> erroredTiles = Collections.newSetFromMap(new WeakHashMap<>());
    private final transient Set<Entity> erroredEntities = Collections.newSetFromMap(new WeakHashMap<>());
    private final boolean fixedPaletteIndex;
    private int paletteIndex;
    private float maxX;
    private float maxY;

    public TemplateRenderer(StructureTemplate template, float maxSize) {
        this(template, maxSize, maxSize, -1);
    }

    public TemplateRenderer(StructureTemplate template, float maxSize, int fixedPaletteIndex) {
        this(template, maxSize, maxSize, fixedPaletteIndex);
    }

    public TemplateRenderer(StructureTemplate template, float maxX, float maxY, int fixedPaletteIndex) {
        this.template = template;
        this.maxX = maxX;
        this.maxY = maxY;
        this.fixedPaletteIndex = fixedPaletteIndex != -1;
        this.paletteIndex = this.fixedPaletteIndex ? fixedPaletteIndex : 0;
    }

    public void setSize(float size) {
        this.setSize(size, size);
    }

    public void setSize(float maxX, float maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void render(GuiGraphics guiGraphics, int xPos, int yPos) {
        Vec3i size = this.template.getSize();
        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();
        float diagonal = (float) Math.sqrt(sizeX * sizeX + sizeZ * sizeZ);
        float scaleX = this.maxX / diagonal;
        float scaleY = this.maxY / sizeY;
        float scale = -Math.min(scaleX, scaleY);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(xPos, yPos, 100);
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.pose().translate(-(float) sizeX / 2, -(float) sizeY / 2, 0);

        // Initial eye pos somewhere off in the distance in the -Z direction
        Vector4f eye = new Vector4f(0, 0, -100, 1);
        Matrix4f rotMat = new Matrix4f();
        rotMat.identity();

        // For each GL rotation done, track the opposite to keep the eye pos accurate
        guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(-30F));
        rotMat.rotation(Axis.XP.rotationDegrees(30));

        float offX = (float) -sizeX / 2;
        float offZ = (float) -sizeZ / 2 + 1;

        float time = ClientTickHandler.ticksInGame();
        guiGraphics.pose().translate(-offX, 0, -offZ);
        guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(time));
        rotMat.rotation(Axis.YP.rotationDegrees(-time));
        guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(45));
        rotMat.rotation(Axis.YP.rotationDegrees(-45));
        guiGraphics.pose().translate(offX, 0, offZ);

        // Finally apply the rotations
        eye.mul(rotMat);
        this.renderElements(guiGraphics, this.template);

        guiGraphics.pose().popPose();
        if (ClientTickHandler.ticksInGame() % 40 == 0) {
            if (this.fixedPaletteIndex) {
                return;
            }

            this.paletteIndex++;
            if (this.paletteIndex >= this.template.palettes.size()) {
                this.paletteIndex = 0;
            }
        }
    }

    private void renderElements(GuiGraphics guiGraphics, StructureTemplate template) {
        guiGraphics.pose().pushPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        guiGraphics.pose().translate(0, 0, -1);

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        this.doWorldRenderPass(guiGraphics, template, buffers);
        this.doTileEntityRenderPass(guiGraphics, template, buffers);
        this.doEntityRenderPass(guiGraphics, template, buffers);

        buffers.endBatch();
        guiGraphics.pose().popPose();
    }

    private void doWorldRenderPass(GuiGraphics guiGraphics, StructureTemplate template, MultiBufferSource.BufferSource buffers) {
        StructureTemplate.Palette palette = template.palettes.get(this.paletteIndex);
        for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
            BlockPos pos = blockInfo.pos();
            BlockState bs = blockInfo.state();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(pos.getX(), pos.getY(), pos.getZ());
            this.renderForMultiblock(bs, pos, guiGraphics, buffers);
            guiGraphics.pose().popPose();
        }
    }

    private void renderForMultiblock(BlockState state, BlockPos pos, GuiGraphics guiGraphics, MultiBufferSource.BufferSource buffers) {
        if (state.getRenderShape() == RenderShape.MODEL) {
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            BakedModel model = blockRenderer.getBlockModel(state);
            for (RenderType layer : model.getRenderTypes(state, this.clientLevel.random, ModelData.EMPTY)) {
                guiGraphics.pose().pushPose();
                Lighting.setupForFlatItems();
                Vec3 vec3 = state.getOffset(this.clientLevel, pos);
                guiGraphics.pose().translate(vec3.x, vec3.y, vec3.z);
                blockRenderer.renderSingleBlock(state, guiGraphics.pose(), buffers, (int) (LightTexture.FULL_BLOCK * 0.8), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, layer);
                guiGraphics.pose().popPose();
            }
        }
    }

    private void doTileEntityRenderPass(GuiGraphics guiGraphics, StructureTemplate template, MultiBufferSource buffers) {
        StructureTemplate.Palette palette = template.palettes.get(this.paletteIndex);
        for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
            BlockPos pos = blockInfo.pos();
            BlockState state = blockInfo.state();

            BlockEntity te = null;
            if (state.getBlock() instanceof EntityBlock) {
                te = this.teCache.computeIfAbsent(pos.immutable(), p -> ((EntityBlock) state.getBlock()).newBlockEntity(pos, state));
            }

            if (te != null && !this.erroredTiles.contains(te)) {
                te.setLevel(this.clientLevel);

                // fake cached state in case the renderer checks it as we don't want to query the actual world
                //noinspection deprecation
                te.setBlockState(state);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(pos.getX(), pos.getY(), pos.getZ());
                try {
                    BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(te);
                    if (renderer != null) {
                        renderer.render(te, 0, guiGraphics.pose(), buffers, LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY);
                    }
                } catch (Exception e) {
                    this.erroredTiles.add(te);
                    SkyblockBuilder.getLogger().error("An exception occurred rendering tile entity", e);
                } finally {
                    guiGraphics.pose().popPose();
                }
            }
        }
    }

    private void doEntityRenderPass(GuiGraphics guiGraphics, StructureTemplate template, MultiBufferSource buffers) {
        for (StructureTemplate.StructureEntityInfo entityInfo : template.entityInfoList) {
            Optional<Entity> maybe = EntityType.create(entityInfo.nbt, this.clientLevel);
            if (maybe.isPresent()) {
                Vec3 pos = entityInfo.pos;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(pos.x(), pos.y(), pos.z());

                Entity entity = maybe.get();
                if (this.erroredEntities.contains(entity)) {
                    continue;
                }

                try {
                    EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                    entityRenderDispatcher.prepare(this.clientLevel, Minecraft.getInstance().gameRenderer.getMainCamera(), entity);
                    EntityRenderer<? super Entity> renderer = entityRenderDispatcher.getRenderer(entity);
                    renderer.render(entity, entity.getYRot(), 0, guiGraphics.pose(), buffers, LightTexture.pack(15, 15));
                } catch (Exception e) {
                    this.erroredEntities.add(entity);
                    SkyblockBuilder.getLogger().error("An exception occurred rendering entity", e);
                } finally {
                    guiGraphics.pose().popPose();
                }
            }
        }
    }
}
