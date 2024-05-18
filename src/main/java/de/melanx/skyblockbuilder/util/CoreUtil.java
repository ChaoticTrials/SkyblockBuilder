package de.melanx.skyblockbuilder.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.DimensionsConfig;
import de.melanx.skyblockbuilder.template.NetherPortalTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.world.presets.SkyblockPreset;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.moddingx.libx.codec.MoreCodecs;

import java.util.Optional;

public class CoreUtil {

    public static Codec<WorldPreset> augmentWorldPresetCodec(Codec<WorldPreset> codec) {
        Codec<SkyblockPreset> skyblockCodecBase = RecordCodecBuilder.create(instance -> instance.group(
                RegistryOps.retrieveGetter(Registries.DIMENSION_TYPE),
                RegistryOps.retrieveGetter(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST),
                RegistryOps.retrieveGetter(Registries.NOISE_SETTINGS),
                RegistryOps.retrieveRegistryLookup(Registries.BIOME).forGetter(SkyblockPreset::getBiomes)
        ).apply(instance, SkyblockPreset::new));

        MapCodec<Unit> skyblockCodecId = Codec.STRING.fieldOf("type").flatXmap(
                str -> "skyblockbuilder:skyblock".equals(str) ? DataResult.success(Unit.INSTANCE) : DataResult.error(() -> "Wrong type"),
                unit -> DataResult.success("skyblockbuilder:skyblock")
        );

        Codec<SkyblockPreset> skyblockCodec = MoreCodecs.extend(skyblockCodecBase, skyblockCodecId, preset -> Pair.of(preset, Unit.INSTANCE), (preset, unit) -> preset);

        return new SkyblockPresetCodec(codec, skyblockCodec);
    }

    public static Optional<BlockUtil.FoundRectangle> getExitPortal(ServerPlayer player, ServerLevel destination, BlockPos findFrom, boolean isToNether, WorldBorder worldBorder) {
        Direction.Axis portalAxis = player.level().getBlockState(player.portalEntrancePos).getOptionalValue(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
        if (!isToNether || DimensionsConfig.Nether.netherPortalStructure.isEmpty()) { // handle vanilla logic
            Optional<BlockUtil.FoundRectangle> portal = destination.getPortalForcer().createPortal(findFrom, portalAxis);
            if (portal.isEmpty()) {
                SkyblockBuilder.getLogger().error("Unable to create a portal, likely target out of worldborder");
            }

            return portal;
        }

        Direction dir = Direction.get(Direction.AxisDirection.POSITIVE, portalAxis);
        Rotation rotation = dir == Direction.SOUTH ? Rotation.CLOCKWISE_90 : Rotation.NONE;

        NetherPortalTemplate netherPortalTemplate = DimensionsConfig.Nether.netherPortalStructure.get();
        BlockPos.MutableBlockPos startPos = findFrom.offset(netherPortalTemplate.getPortalOffset().rotate(rotation)).mutable();

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
                TemplateLoader.STRUCTURE_PLACE_SETTINGS.copy().setRotation(rotation),
                destination.random,
                Block.UPDATE_ALL);

        return !worldBorder.isWithinBounds(startPos) ? Optional.empty() : Optional.of(new BlockUtil.FoundRectangle(startPos.offset(netherPortalTemplate.getPortalOffset().multiply(-1).rotate(rotation)), 2, 3));
    }

    private record SkyblockPresetCodec(Codec<WorldPreset> base, Codec<SkyblockPreset> skyblock) implements Codec<WorldPreset> {

        @Override
        public <T> DataResult<T> encode(WorldPreset input, DynamicOps<T> ops, T prefix) {
            if (input instanceof SkyblockPreset skyblockPreset) {
                return this.skyblock().encode(skyblockPreset, ops, prefix);
            } else {
                return this.base().encode(input, ops, prefix);
            }
        }

        @Override
        public <T> DataResult<Pair<WorldPreset, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<SkyblockPreset, T>> skyblockResult = this.skyblock().decode(ops, input);
            if (skyblockResult.result().isPresent()) {
                //noinspection unchecked
                return (DataResult<Pair<WorldPreset, T>>) (DataResult<?>) skyblockResult;
            } else {
                return this.base().decode(ops, input);
            }
        }
    }
}
