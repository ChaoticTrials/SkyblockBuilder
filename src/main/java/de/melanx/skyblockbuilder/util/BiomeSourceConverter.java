package de.melanx.skyblockbuilder.util;

import com.mojang.datafixers.util.Pair;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import org.moddingx.libx.util.data.ResourceList;

import java.util.*;
import java.util.stream.Collectors;

public class BiomeSourceConverter {

    public static BiomeSource customBiomeSource(ResourceKey<Level> level, BiomeSource baseSource, HolderLookup<Biome> biomes) {
        ResourceList resourceList = WorldConfig.biomes.get(level.location().toString());
        if (resourceList != null) {
            Set<Holder<Biome>> newBiomes = new HashSet<>();
            for (Holder<Biome> possibleBiome : baseSource.possibleBiomes()) {
                Optional<ResourceKey<Biome>> optionalResourceKey = possibleBiome.unwrapKey();
                optionalResourceKey.ifPresent(key -> {
                    ResourceLocation location = key.location();
                    if (resourceList.test(location)) {
                        newBiomes.add(possibleBiome);
                    }
                });
            }

            if (newBiomes.isEmpty()) {
                biomes.listElementIds().filter(biomeKey -> resourceList.test(biomeKey.location())).forEach(key -> newBiomes.add(biomes.getOrThrow(key)));
            } else {
                SkyblockBuilder.getLogger().warn("Skipping biome filtering as all biomes were filtered out: {}", level);
                newBiomes.addAll(baseSource.possibleBiomes());
            }

            if (newBiomes.isEmpty()) {
                throw new IllegalStateException("No biomes selected for " + level);
            }

            if (baseSource instanceof MultiNoiseBiomeSource) {
                List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameters = new ArrayList<>(((MultiNoiseBiomeSource) baseSource).parameters().values()).stream().filter(pair -> {
                    Holder<Biome> holder = pair.getSecond();
                    Optional<ResourceKey<Biome>> optionalResourceKey = holder.unwrapKey();
                    return optionalResourceKey.filter(biomeResourceKey -> resourceList.test(biomeResourceKey.location())).isPresent();
                }).collect(Collectors.toList());
                if (parameters.isEmpty()) {
                    newBiomes.forEach(holder -> {
                        parameters.add(Pair.of(new Climate.ParameterPoint(Climate.Parameter.point(0), Climate.Parameter.point(0), Climate.Parameter.point(0), Climate.Parameter.point(0), Climate.Parameter.point(0), Climate.Parameter.point(0), 0L), holder));
                    });
                }
                return MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(parameters));
            }

            if (baseSource instanceof TheEndBiomeSource) {
                if (newBiomes.size() == 5) {
                    List<Holder<Biome>> newBiomesList = newBiomes.stream().toList();
                    return new TheEndBiomeSource(newBiomesList.get(0), newBiomesList.get(1), newBiomesList.get(2), newBiomesList.get(3), newBiomesList.get(4));
                }

                SkyblockBuilder.getLogger().warn("Need to be exactly 5 biomes for '{}', currently {}", level, newBiomes.size());
            }

            SkyblockBuilder.getLogger().warn("Unable to modify dimension '{}' properly, using default", level);
        }

        return baseSource;
    }
}
