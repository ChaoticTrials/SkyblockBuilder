package de.melanx.skyblockbuilder.template;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.FakeLevel;
import de.melanx.skyblockbuilder.config.common.ClientConfig;
import de.melanx.skyblockbuilder.util.SkyComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.apache.commons.lang3.Validate;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.moddingx.libx.render.ClientTickHandler;

import java.io.FileInputStream;
import java.util.*;

/*
 * Code adapted from Patchouli by Vazkii
 * Modified by MelanX for use in SkyblockBuilder
 * Original source: https://github.com/VazkiiMods/Patchouli/blob/35ae32b6b9c9c37a78ecd4867b83ba25304fd0c7/Common/src/main/java/vazkii/patchouli/client/book/page/PageMultiblock.java
 *
 * This file is subject to the same license as Patchouli.
 * See: https://github.com/VazkiiMods/Patchouli/blob/HEAD/LICENSE
 */
public class TemplatePreviewRenderer {

    private final ClientLevel clientLevel;
    private final TemplatePreview preview;
    private final transient Map<BlockPos, BlockEntity> teCache = new HashMap<>();
    private final transient Map<StructureTemplate.StructureEntityInfo, Entity> entityCache = new HashMap<>();
    private final transient Set<BlockEntity> erroredTiles = Collections.newSetFromMap(new WeakHashMap<>());
    private final transient Set<Entity> erroredEntities = Collections.newSetFromMap(new WeakHashMap<>());
    private final transient Set<StructureTemplate.StructureEntityInfo> loadFailedEntities = Collections.newSetFromMap(new WeakHashMap<>());
    private final transient Set<StructureTemplate.StructureEntityInfo> erroredEntityInfos = Collections.newSetFromMap(new WeakHashMap<>());
    private final boolean fixedPaletteIndex;
    private final boolean aprilUpsideDown;
    private int paletteIndex;
    private Area area;
    private DynamicTexture icon;
    private long lastTick;

    public TemplatePreviewRenderer(TemplatePreview preview, Area area) {
        this(preview, area, Optional.ofNullable(Minecraft.getInstance().level).orElseThrow(() -> new IllegalArgumentException("Consider using another constructor")).registryAccess());
    }

    public TemplatePreviewRenderer(TemplatePreview preview, Area area, RegistryAccess registryAccess) {
        this(preview, area, registryAccess, -1);
    }

    public TemplatePreviewRenderer(TemplatePreview preview, Area area, RegistryAccess registryAccess, int fixedPaletteIndex) {
        Calendar calendar = Calendar.getInstance();
        this.clientLevel = Optional.ofNullable(Minecraft.getInstance().level).orElse(FakeLevel.getInstance(registryAccess));
        this.aprilUpsideDown = ClientConfig.allowAprilFools && calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1;
        this.preview = preview;
        this.area = area;
        this.fixedPaletteIndex = fixedPaletteIndex != -1;
        this.paletteIndex = this.fixedPaletteIndex ? fixedPaletteIndex : 0;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    protected void renderTemplate(GuiGraphics guiGraphics) {
        StructureTemplate template = this.preview.getRenderingTemplatePath();
        Vec3i size = template.getSize();
        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();
        float diagonal = (float) Math.sqrt(sizeX * sizeX + sizeZ * sizeZ);
        float scaleX = this.area.width() / diagonal;
        float scaleY = (float) this.area.height() / diagonal * 0.88F;
        float scale = -Math.min(scaleX, scaleY);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.area.minX + (float) this.area.width() / 2, this.area.minY + (float) this.area.height() / 2, 1000);
        guiGraphics.pose().scale(scale, scale, scale);
        if (ClientConfig.allowAprilFools && this.aprilUpsideDown) {
            guiGraphics.pose().scale(1, -1, -1);
        }
        guiGraphics.pose().translate(-(float) sizeX / 2, -(float) sizeY / 2, 0);

        // Initial eye pos somewhere off in the distance in the -Z direction
        Vector4f eye = new Vector4f(0, 0, -1000, 1);
        Matrix4f rotMat = new Matrix4f();
        rotMat.identity();

        // For each GL rotation done, track the opposite to keep the eye pos accurate
        guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(-30F));
        rotMat.rotation(Axis.XP.rotationDegrees(30));
        guiGraphics.pose().translate(0, -sizeZ * 0.16F, 0);

        float offX = (float) -sizeX / 2;
        float offZ = (float) -sizeZ / 2 + 1;

        float time = ClientTickHandler.ticksInGame();
        guiGraphics.pose().translate(-offX, 0, -offZ);
        guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(time));
        rotMat.rotation(Axis.YP.rotationDegrees(-time));
        guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(45));
        rotMat.rotation(Axis.YP.rotationDegrees(-45));
        guiGraphics.pose().translate(offX, 0, offZ);

        // Finally, apply the rotations
        eye.mul(rotMat);
        this.renderElements(guiGraphics, template);

        guiGraphics.pose().popPose();
        if (ClientTickHandler.ticksInGame() % 40 == 0 && ClientTickHandler.ticksInGame() != this.lastTick) {
            this.lastTick = ClientTickHandler.ticksInGame();
            if (this.fixedPaletteIndex) {
                return;
            }

            this.paletteIndex++;
            if (this.paletteIndex >= template.palettes.size()) {
                this.paletteIndex = 0;
            }
        }
    }

    protected void renderIcon(GuiGraphics guiGraphics) {
        if (this.icon == null) {
            this.loadIcon();
        }

        if (this.icon == null) {
            return;
        }

        NativeImage imagePixels = this.icon.getPixels();
        if (imagePixels == null) {
            return;
        }

        //noinspection ConstantConditions
        int iconSize = imagePixels.getHeight();
        int renderSize = Math.min(this.area.width(), this.area.height());

        int x = this.area.width() < this.area.height() ? this.area.minX : this.area.minX + (this.area.maxX / 2) - (renderSize / 2);
        int y = this.area.width() > this.area.height() ? this.area.minY : this.area.minY + (this.area.maxY / 2) - (renderSize / 2);
        guiGraphics.blit(this.preview.getIcon().location(), x, y, renderSize, renderSize, 0, 0, iconSize, iconSize, iconSize, iconSize);
    }

    public void render(GuiGraphics guiGraphics) {
        if (this.preview.getType() == TemplatePreview.PreviewType.IMAGE) {
            this.renderIcon(guiGraphics);
        } else {
            this.renderTemplate(guiGraphics);
            if (!this.erroredTiles.isEmpty() || !this.erroredEntities.isEmpty() || !this.erroredEntityInfos.isEmpty() || !this.loadFailedEntities.isEmpty()) {
                guiGraphics.drawWordWrap(Minecraft.getInstance().font, SkyComponents.SCREEN_ERROR_LOAD_TEMPLATE, 5, this.area.minY, this.area.maxX - 10, 0xFFFFFF);
            }
        }
    }

    private void renderElements(GuiGraphics guiGraphics, StructureTemplate template) {
        guiGraphics.pose().pushPose();
        ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
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
            if (this.erroredEntityInfos.contains(entityInfo)) {
                continue;
            }

            Entity entity;
            try {
                entity = this.entityCache.computeIfAbsent(entityInfo, info -> {
                    Optional<Entity> maybe = EntityType.by(info.nbt).map(type -> type.create(this.clientLevel));
                    if (maybe.isEmpty()) {
                        SkyblockBuilder.getLogger().error("Could not create entity of type {}", info.nbt.getString("id"));
                        this.erroredEntityInfos.add(info);
                        return null;
                    }

                    Entity e = maybe.get();
                    try {
                        try {
                            e.load(info.nbt);
                        } catch (Exception ex) {
                            this.setPosition(e, info.nbt);
                            this.loadFailedEntities.add(info);
                            SkyblockBuilder.getLogger().error("An exception occurred loading entity: {}", String.valueOf(ex));
                        }

                        return e;
                    } catch (Exception ex) {
                        this.loadFailedEntities.add(info);
                        SkyblockBuilder.getLogger().error("An exception occurred creating entity", ex);
                        return null;
                    }
                });

                if (entity == null) {
                    continue;
                }
            } catch (Exception e) {
                this.loadFailedEntities.add(entityInfo);
                SkyblockBuilder.getLogger().error("An exception occurred creating entity", e);
                continue;
            }

            Vec3 pos = entityInfo.pos;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(pos.x(), pos.y(), pos.z());
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

    private void setPosition(Entity entity, CompoundTag compound) {
        ListTag pos = compound.getList("Pos", CompoundTag.TAG_DOUBLE);
        ListTag rot = compound.getList("Rotation", CompoundTag.TAG_FLOAT);
        double maxHorizontalPosition = 30_000_000;
        entity.setPosRaw(
                Mth.clamp(pos.getDouble(0), -maxHorizontalPosition, maxHorizontalPosition),
                Mth.clamp(pos.getDouble(1), -20_000_000, 20_000_000),
                Mth.clamp(pos.getDouble(2), -maxHorizontalPosition, maxHorizontalPosition)
        );
        entity.setYRot(rot.getFloat(0));
        entity.setXRot(rot.getFloat(1));
    }

    private void loadIcon() {
        ResourceLocation iconLocation = this.preview.getIcon().location();
        if (this.preview.getType() != TemplatePreview.PreviewType.IMAGE) {
            Minecraft.getInstance().textureManager.release(iconLocation);
            this.icon = null;
            return;
        }

        try {
            FileInputStream in = new FileInputStream(this.preview.getIcon().path().toFile());

            DynamicTexture texture;
            try {
                NativeImage image = NativeImage.read(in);
                Validate.validState(image.getWidth() == image.getHeight(), "Height and width must be equal.");
                DynamicTexture tempTexture = new DynamicTexture(image);
                if (this.fixedPaletteIndex) {
                    iconLocation = iconLocation.withSuffix("_" + this.paletteIndex);
                }
                Minecraft.getInstance().textureManager.register(iconLocation, tempTexture);
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
            this.icon = texture;
        } catch (Throwable throwable) {
            SkyblockBuilder.getLogger().error("Invalid icon for template {}", this.preview, throwable);
        }
    }

    public record Area(int minX, int minY, int maxX, int maxY) {

        public Area(int maxX, int maxY) {
            this(0, 0, maxX, maxY);
        }

        public Area(int maxSize) {
            this(maxSize, maxSize);
        }

        public int width() {
            return this.maxX - this.minX;
        }

        public int height() {
            return this.maxY - this.minY;
        }
    }
}
