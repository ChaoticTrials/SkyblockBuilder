package de.melanx.skyblockbuilder.mixin;

import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.NetherPortalTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.TemplateUtil;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {

    @Inject(
            method = "getExitPortal",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getExitPortal(ServerLevel destination, Entity entity, BlockPos pos, BlockPos exitPos, boolean isToNether, WorldBorder worldBorder, CallbackInfoReturnable<DimensionTransition> cir) {
        if (!isToNether || TemplateLoader.getNetherPortalTemplate() == null) {
            return;
        }

        if (destination.getPortalForcer().findClosestPortalPosition(exitPos, isToNether, worldBorder).isPresent()) {
            return;
        }

        Direction.Axis portalAxis = entity.level().getBlockState(pos).getOptionalValue(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
        Direction dir = Direction.get(Direction.AxisDirection.POSITIVE, portalAxis);
        Rotation rotation = dir == Direction.SOUTH ? Rotation.CLOCKWISE_90 : Rotation.NONE;

        NetherPortalTemplate netherPortalTemplate = TemplateLoader.getNetherPortalTemplate();
        BlockPos.MutableBlockPos startPos = exitPos.offset(netherPortalTemplate.getPortalOffset().rotate(rotation)).mutable();

        BlockPos.MutableBlockPos topPos = startPos.immutable().above(netherPortalTemplate.getStructure().size.getY()).mutable();
        int logicalBuildHeight = destination.getMinBuildHeight() + destination.getLogicalHeight();
        if (logicalBuildHeight < topPos.getY()) {
            topPos.setY(logicalBuildHeight);
            int i = 1;
            while (destination.getBlockState(topPos).is(Blocks.BEDROCK)) {
                topPos.move(Direction.DOWN);
                i++;
            }
            startPos.setY(logicalBuildHeight - netherPortalTemplate.getStructure().size.getY() - i);
        }

        if (destination.getMinBuildHeight() > startPos.getY()) {
            startPos.setY(destination.getMinBuildHeight());
            while (destination.getBlockState(startPos).is(Blocks.BEDROCK)) {
                startPos.move(Direction.UP);
            }
        }
        netherPortalTemplate.getStructure().placeInWorld(destination,
                startPos, startPos,
                TemplateUtil.STRUCTURE_PLACE_SETTINGS.copy().setRotation(rotation),
                destination.random,
                Block.UPDATE_ALL);

        BlockPos portalBlockPos = startPos.offset(netherPortalTemplate.getPortalOffset().multiply(-1).rotate(rotation));

        if (!TemplatesConfig.netherSpreads.spreads().isEmpty()) {
            SkyblockSavedData data = SkyblockSavedData.get(destination.getServer().overworld());
            Team team = data.getTeamFromPlayer(entity.getUUID());
            if (team == null) {
                Team spawn = data.getSpawn();
                if (spawn.getPlayers().contains(entity.getUUID())) {
                    team = spawn;
                }
            }

            ConfiguredTemplate.placeNetherSpreads(TemplatesConfig.netherSpreads, destination, team, portalBlockPos, destination.random, Block.UPDATE_ALL);
        }

        if (!worldBorder.isWithinBounds(startPos)) {
            cir.setReturnValue(null);
        }

        BlockUtil.FoundRectangle rectangle = new BlockUtil.FoundRectangle(portalBlockPos, 2, 3);
        cir.setReturnValue(NetherPortalBlock.getDimensionTransitionFromExit(entity, pos, rectangle, destination, DimensionTransition.PLAY_PORTAL_SOUND));
    }
}
