package de.melanx.skyblockbuilder.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.events.SkyblockHooks;
import de.melanx.skyblockbuilder.world.presets.SkyblockPreset;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.moddingx.libx.codec.MoreCodecs;

import javax.annotation.Nullable;

public class CoreUtil {

    @Nullable
    public static ServerLevel resolveLevel(MinecraftServer server, ResourceKey<Level> dimension, ServerPlayer player) {
        return server.getLevel(SkyblockHooks.onChangeDimension(player, dimension));
    }

    public static boolean redirectToTeamIsland(ServerPlayer player, ResourceKey<Level> dimension) {
        if (SkyblockHooks.onChangeDimension(player, dimension) == dimension) {
            return false;
        }

        Team team = SkyblockSavedData.get(player.level()).getTeamFromPlayer(player);
        if (team == null) {
            return false;
        }

        WorldUtil.teleportToIsland(player, team);

        return true;
    }

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
