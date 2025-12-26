package de.melanx.skyblockbuilder.world.presets;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.moddingx.libx.util.data.ResourceList;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class BiomeParametersPreset implements MultiNoiseBiomeSourceParameterList.Preset.SourceProvider {

    public static final MultiNoiseBiomeSourceParameterList.Preset FILTERED_OVERWORLD = new MultiNoiseBiomeSourceParameterList.Preset(
            SkyblockBuilder.getInstance().resource("filtered_overworld"), new BiomeParametersPreset());

    private static HolderLookup.RegistryLookup<Biome> BIOMES;

    public static void init(HolderLookup.RegistryLookup<Biome> biomes) {
        BiomeParametersPreset.BIOMES = biomes;
    }

    @Nonnull
    @Override
    public <T> Climate.ParameterList<T> apply(@Nonnull Function<ResourceKey<Biome>, T> valueGetter) {
        return this.generateOverworldBiomes(valueGetter);
    }

    private <T> Climate.ParameterList<T> generateOverworldBiomes(Function<ResourceKey<Biome>, T> valueGetter) {
        ImmutableList.Builder<Pair<Climate.ParameterPoint, T>> builder = ImmutableList.builder();
        ResourceList resourceList = WorldConfig.biomes.getOrDefault(Level.OVERWORLD.location().toString(), ResourceList.DENY_LIST);
        Set<ResourceKey<Biome>> addedBiomes = new HashSet<>();
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer = (p -> {
            if (resourceList.test(p.getSecond().location())) {
                builder.add(p.mapSecond(valueGetter));
                addedBiomes.add(p.getSecond());
            }
        });

        new OverworldBiomeBuilder().addBiomes(consumer);
        if (BIOMES == null) {
            return new Climate.ParameterList<>(builder.build());
        }

        BIOMES.listElementIds().forEach(biomeResourceKey -> {
            Optional<Holder.Reference<Biome>> biomeReference = BIOMES.get(biomeResourceKey);
            if (!addedBiomes.contains(biomeResourceKey) && biomeReference.isPresent() && resourceList.test(biomeResourceKey.location())) {
                builder.add(Pair.of(WorldUtil.pointFor(biomeResourceKey), valueGetter.apply(biomeResourceKey)));
            }
        });

        return new Climate.ParameterList<>(builder.build());
    }
}
