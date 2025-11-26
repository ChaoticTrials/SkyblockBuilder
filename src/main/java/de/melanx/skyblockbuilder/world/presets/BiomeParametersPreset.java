package de.melanx.skyblockbuilder.world.presets;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.moddingx.libx.util.data.ResourceList;

import java.util.function.Consumer;
import java.util.function.Function;

public class BiomeParametersPreset {

    public static final MultiNoiseBiomeSourceParameterList.Preset FILTERED_OVERWORLD = new MultiNoiseBiomeSourceParameterList.Preset(
            SkyblockBuilder.getInstance().resource("filtered_overworld"), BiomeParametersPreset::generateOverworldBiomes
    );

    private static <T> Climate.ParameterList<T> generateOverworldBiomes(Function<ResourceKey<Biome>, T> valueGetter) {
        ImmutableList.Builder<Pair<Climate.ParameterPoint, T>> builder = ImmutableList.builder();
        ResourceList resourceList = WorldConfig.biomes.getOrDefault(Level.OVERWORLD.location().toString(), ResourceList.DENY_LIST);
        Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer = (p -> {
            if (resourceList.test(p.getSecond().location())) {
                builder.add(p.mapSecond(valueGetter));
            }
        });

        new OverworldBiomeBuilder().addBiomes(consumer);

        return new Climate.ParameterList<>(builder.build());
    }
}
