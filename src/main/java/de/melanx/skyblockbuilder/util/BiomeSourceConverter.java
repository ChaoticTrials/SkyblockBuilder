package de.melanx.skyblockbuilder.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;

public class BiomeSourceConverter {

    public static BiomeSource customBiomeSource(ResourceKey<Level> level, BiomeSource baseSource) {
//        ResourceList resourceList = ConfigHandler.World.biomes.get(level.location().toString()); todo
//        if (resourceList != null) {
//            Set<Holder<Biome>> newBiomes = new HashSet<>();
//            for (Holder<Biome> possibleBiome : baseSource.possibleBiomes()) {
//                Optional<ResourceKey<Biome>> optionalResourceKey = possibleBiome.unwrapKey();
//                optionalResourceKey.ifPresent(key -> {
//                    ResourceLocation location = key.location();
//                    if (resourceList.test(location)) {
//                        newBiomes.add(possibleBiome);
//                    }
//                });
//            }
//
//            if (newBiomes.isEmpty()) {
//                ForgeRegistries.BIOMES.getEntries().stream().map(Map.Entry::getKey).filter(key -> resourceList.test(key.location())).forEach(key -> {
//                    Optional<Holder<Biome>> optHolder = ForgeRegistries.BIOMES.getHolder(key);
//                    optHolder.ifPresent(newBiomes::add);
//                });
//            }
//
//            if (newBiomes.isEmpty()) {
//                throw new IllegalStateException("No biomes selected for " + level);
//            }
//
//            if (baseSource instanceof MultiNoiseBiomeSource) {
//                List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameters = new ArrayList<>(((MultiNoiseBiomeSource) baseSource).parameters.values()).stream().filter(pair -> {
//                    Holder<Biome> holder = pair.getSecond();
//                    Optional<ResourceKey<Biome>> optionalResourceKey = holder.unwrapKey();
//                    return optionalResourceKey.filter(biomeResourceKey -> resourceList.test(biomeResourceKey.location())).isPresent();
//                }).collect(Collectors.toList());
//                if (parameters.isEmpty()) {
//                    newBiomes.forEach(holder -> {
//                        parameters.add(Pair.of(new Climate.ParameterPoint(Climate.Parameter.point(0), Climate.Parameter.point(0), Climate.Parameter.point(0), Climate.Parameter.point(0), Climate.Parameter.point(0), Climate.Parameter.point(0), 0L), holder));
//                    });
//                }
//                return new MultiNoiseBiomeSource(new Climate.ParameterList<>(parameters), Optional.empty());
//            }
//
//            if (baseSource instanceof TheEndBiomeSource) {
//                if (newBiomes.size() == 5) {
//                    List<Holder<Biome>> newBiomesList = newBiomes.stream().toList();
//                    return new TheEndBiomeSource(newBiomesList.get(0), newBiomesList.get(1), newBiomesList.get(2), newBiomesList.get(3), newBiomesList.get(4));
//                }
//
//                SkyblockBuilder.getLogger().warn("Need to be exactly 5 biomes for '{}', currently {}", level, newBiomes.size());
//            }
//
//            SkyblockBuilder.getLogger().warn("Unable to modify dimension '{}' properly, using default", level);
//        }

        return baseSource;
    }
}
