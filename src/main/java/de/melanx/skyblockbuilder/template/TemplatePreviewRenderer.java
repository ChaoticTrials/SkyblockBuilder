package de.melanx.skyblockbuilder.template;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.FakeLevel;
import de.melanx.skyblockbuilder.client.render.TemplatePreviewPipRenderer;
import de.melanx.skyblockbuilder.client.render.TemplatePreviewRenderState;
import de.melanx.skyblockbuilder.config.common.ClientConfig;
import de.melanx.skyblockbuilder.util.SkyComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
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

    private static final float COS_30 = Mth.cos(30F * Mth.DEG_TO_RAD);
    private static final float SIN_30 = 0.5F;
    private static final Matrix4fc NO_TRANSFORMATION = new Matrix4f();

    private final ClientLevel clientLevel;
    private final TemplatePreview preview;
    private final transient Map<BlockPos, BlockEntity> teCache = new HashMap<>();
    private final transient Map<StructureTemplate.StructureEntityInfo, Entity> entityCache = new HashMap<>();
    private final transient Set<BlockEntity> erroredTiles = Collections.newSetFromMap(new WeakHashMap<>());
    private final transient Set<UUID> erroredEntities = Collections.newSetFromMap(new WeakHashMap<>());
    private final transient Set<StructureTemplate.StructureEntityInfo> loadFailedEntities = Collections.newSetFromMap(new WeakHashMap<>());
    private final transient Set<StructureTemplate.StructureEntityInfo> erroredEntityInfos = Collections.newSetFromMap(new WeakHashMap<>());
    private final transient BlockModelRenderState blockModelRenderState = new BlockModelRenderState();
    private final boolean fixedPaletteIndex;
    private final boolean aprilUpsideDown;
    private int paletteIndex;
    private Area area;
    private DynamicTexture icon;
    private Identifier iconLocation;
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

    /**
     * Draws the structure into the picture in picture texture. Called by the
     * {@link TemplatePreviewPipRenderer}, the pose stack is already
     * centered in the target area and scaled to gui pixels.
     */
    public void renderTemplate(PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        StructureTemplate template = this.preview.getRenderingTemplatePath();
        Vec3i size = template.getSize();
        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();

        float radius = (float) Math.sqrt(sizeX * sizeX + sizeZ * sizeZ) / 2F + 1; // +1 covers the offZ pivot offset
        float projectedWidth = radius * 2F;
        float projectedHeight = sizeY * COS_30 + radius * 2F * SIN_30 + sizeZ * 0.16F * COS_30;
        float scale = Math.min(this.area.width() / projectedWidth, this.area.height() / projectedHeight);

        poseStack.pushPose();
        // the picture in picture pipeline already mirrors the z axis, so only x and y need to be flipped here
        poseStack.scale(-scale, -scale, scale);
        if (ClientConfig.allowAprilFools && this.aprilUpsideDown) {
            poseStack.scale(1, -1, -1);
        }
        poseStack.translate(-(float) sizeX / 2, -(float) sizeY / 2, 0);

        poseStack.mulPose(Axis.XP.rotationDegrees(-30F));
        poseStack.translate(0, -sizeZ * 0.16F, 0);

        float offX = (float) -sizeX / 2;
        float offZ = (float) -sizeZ / 2 + 1;

        float time = ClientTickHandler.ticksInGame();
        poseStack.translate(-offX, 0, -offZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(time));
        poseStack.mulPose(Axis.YP.rotationDegrees(45));
        poseStack.translate(offX, 0, offZ);

        this.renderElements(poseStack, submitNodeCollector, template);

        poseStack.popPose();
    }

    private void renderIcon(GuiGraphicsExtractor guiGraphics) {
        if (this.icon == null) {
            this.loadIcon();
        }

        if (this.icon == null) {
            return;
        }

        NativeImage imagePixels = this.icon.getPixels();
        //noinspection ConstantValue
        if (imagePixels == null) {
            return;
        }

        int iconSize = imagePixels.getHeight();
        int renderSize = Math.min(this.area.width(), this.area.height());

        int x = this.area.width() < this.area.height() ? this.area.minX : this.area.minX + (this.area.maxX / 2) - (renderSize / 2);
        int y = this.area.width() > this.area.height() ? this.area.minY : this.area.minY + (this.area.maxY / 2) - (renderSize / 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.iconLocation, x, y, 0, 0, renderSize, renderSize, iconSize, iconSize, iconSize, iconSize);
    }

    public void render(GuiGraphicsExtractor guiGraphics) {
        if (this.preview.getType() == TemplatePreview.PreviewType.IMAGE) {
            this.renderIcon(guiGraphics);
        } else {
            guiGraphics.submitPictureInPictureRenderState(new TemplatePreviewRenderState(
                    this,
                    this.area.minX, this.area.minY, this.area.maxX, this.area.maxY,
                    1,
                    new Matrix3x2f(guiGraphics.pose()),
                    guiGraphics.peekScissorStack()
            ));

            this.tickPalette();
            if (!this.erroredTiles.isEmpty() || !this.erroredEntities.isEmpty() || !this.erroredEntityInfos.isEmpty() || !this.loadFailedEntities.isEmpty()) {
                guiGraphics.textWithWordWrap(Minecraft.getInstance().font, SkyComponents.SCREEN_ERROR_LOAD_TEMPLATE, 5, this.area.minY, this.area.maxX - 10, 0xFFFFFF);
            }
        }
    }

    private void tickPalette() {
        if (ClientTickHandler.ticksInGame() % 40 == 0 && ClientTickHandler.ticksInGame() != this.lastTick) {
            this.lastTick = ClientTickHandler.ticksInGame();
            if (this.fixedPaletteIndex) {
                return;
            }

            this.paletteIndex++;
            if (this.paletteIndex >= this.preview.getRenderingTemplatePath().palettes.size()) {
                this.paletteIndex = 0;
            }
        }
    }

    private void renderElements(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, StructureTemplate template) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        CameraRenderState camera = new CameraRenderState();

        this.doWorldRenderPass(poseStack, submitNodeCollector, template);
        this.doTileEntityRenderPass(poseStack, submitNodeCollector, template, camera);
        this.doEntityRenderPass(poseStack, submitNodeCollector, template, camera);
    }

    private void doWorldRenderPass(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, StructureTemplate template) {
        StructureTemplate.Palette palette = template.palettes.get(this.paletteIndex);
        for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
            BlockPos pos = blockInfo.pos();
            BlockState state = blockInfo.state();
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            this.renderForMultiblock(state, pos, poseStack, submitNodeCollector);
            poseStack.popPose();
        }
    }

    private void renderForMultiblock(BlockState state, BlockPos pos, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        if (state.getRenderShape() != RenderShape.MODEL) {
            return;
        }

        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        BlockModelRenderState renderState = this.blockModelRenderState;
        renderState.clear();

        boolean translucent = model.hasMaterialFlag(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, BakedQuad.FLAG_TRANSLUCENT);
        List<BlockStateModelPart> parts = renderState.setupModel(TemplatePreviewRenderer.NO_TRANSFORMATION, translucent);
        model.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, renderState.scratchRandomSource(state.getSeed(pos)), parts);
        for (BlockTintSource tint : Minecraft.getInstance().getBlockColors().getTintSources(state)) {
            renderState.tintLayers().add(tint.color(state));
        }

        if (renderState.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        Vec3 offset = state.getOffset(pos);
        poseStack.translate(offset.x, offset.y, offset.z);
        renderState.submitMultiLayer(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private void doTileEntityRenderPass(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, StructureTemplate template, CameraRenderState camera) {
        BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        StructureTemplate.Palette palette = template.palettes.get(this.paletteIndex);
        for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
            BlockPos pos = blockInfo.pos();
            BlockState state = blockInfo.state();

            BlockEntity te = null;
            if (state.getBlock() instanceof EntityBlock entityBlock) {
                te = this.teCache.computeIfAbsent(pos.immutable(), p -> entityBlock.newBlockEntity(pos, state));
            }

            if (te == null || this.erroredTiles.contains(te)) {
                continue;
            }

            te.setLevel(this.clientLevel);

            // fake cached state in case the renderer checks it as we don't want to query the actual world
            //noinspection deprecation
            te.setBlockState(state);

            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            try {
                this.submitBlockEntity(dispatcher, te, poseStack, submitNodeCollector, camera);
            } catch (Exception e) {
                this.erroredTiles.add(te);
                SkyblockBuilder.getLogger().error("An exception occurred rendering tile entity", e);
            } finally {
                poseStack.popPose();
            }
        }
    }

    private <T extends BlockEntity, S extends BlockEntityRenderState> void submitBlockEntity(BlockEntityRenderDispatcher dispatcher, T blockEntity, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        BlockEntityRenderer<T, S> renderer = dispatcher.getRenderer(blockEntity);
        if (renderer == null) {
            return;
        }

        S renderState = renderer.createRenderState();
        renderer.extractRenderState(blockEntity, renderState, 0, Vec3.ZERO, null);
        renderState.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        renderer.submit(renderState, poseStack, submitNodeCollector, camera);
    }

    private void doEntityRenderPass(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, StructureTemplate template, CameraRenderState camera) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        for (StructureTemplate.StructureEntityInfo entityInfo : template.entityInfoList) {
            if (this.erroredEntityInfos.contains(entityInfo)) {
                continue;
            }

            Entity entity;
            try {
                entity = this.entityCache.computeIfAbsent(entityInfo, this::loadEntity);

                if (entity == null) {
                    continue;
                }
            } catch (Exception e) {
                this.loadFailedEntities.add(entityInfo);
                SkyblockBuilder.getLogger().error("An exception occurred creating entity", e);
                continue;
            }

            if (this.erroredEntities.contains(entity.getUUID())) {
                continue;
            }

            Vec3 pos = entityInfo.pos;
            poseStack.pushPose();
            poseStack.translate(pos.x(), pos.y(), pos.z());

            try {
                EntityRenderState renderState = dispatcher.extractEntity(entity, 0);
                renderState.shadowPieces.clear();
                renderState.outlineColor = EntityRenderState.NO_OUTLINE;
                renderState.lightCoords = LightCoordsUtil.FULL_BRIGHT;
                dispatcher.submit(renderState, camera, 0, 0, 0, poseStack, submitNodeCollector);
            } catch (Exception e) {
                this.erroredEntities.add(entity.getUUID());
                SkyblockBuilder.getLogger().error("An exception occurred rendering entity", e);
            } finally {
                poseStack.popPose();
            }
        }
    }

    private Entity loadEntity(StructureTemplate.StructureEntityInfo info) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(SkyblockBuilder.getLogger())) {
            ValueInput input = TagValueInput.create(reporter, this.clientLevel.registryAccess(), info.nbt);
            Optional<Entity> entity = EntityType.create(input, this.clientLevel, EntitySpawnReason.LOAD);
            if (entity.isEmpty()) {
                SkyblockBuilder.getLogger().error("Could not create entity of type {}", info.nbt.getString("id"));
                this.erroredEntityInfos.add(info);
                return null;
            }

            return entity.get();
        } catch (Exception e) {
            this.loadFailedEntities.add(info);
            SkyblockBuilder.getLogger().error("An exception occurred loading entity", e);
            return null;
        }
    }

    private void loadIcon() {
        Identifier iconLocation = this.preview.getIcon().location();
        if (this.preview.getType() != TemplatePreview.PreviewType.IMAGE) {
            Minecraft.getInstance().textureManager.release(iconLocation);
            this.icon = null;
            this.iconLocation = null;
            return;
        }

        try {
            FileInputStream in = new FileInputStream(this.preview.getIcon().path().toFile());

            DynamicTexture texture;
            try {
                NativeImage image = NativeImage.read(in);
                Validate.validState(image.getWidth() == image.getHeight(), "Height and width must be equal.");
                if (this.fixedPaletteIndex) {
                    iconLocation = iconLocation.withSuffix("_" + this.paletteIndex);
                }
                Identifier registeredLocation = iconLocation;
                DynamicTexture tempTexture = new DynamicTexture(registeredLocation::toString, image);
                Minecraft.getInstance().textureManager.register(registeredLocation, tempTexture);
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
            this.iconLocation = iconLocation;
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
