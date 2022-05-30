package de.melanx.skyblockbuilder.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import de.melanx.skyblockbuilder.config.ConfigHandler;
import io.github.noeppi_noeppi.libx.util.ResourceList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class BiomeSourceConverter {

    public static BiomeSource customBiomeSource(ResourceKey<Level> level, RegistryAccess registryAccess, BiomeSource baseSource) {
        RegistryOps<JsonElement> dynOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        BiomeSource modifiableSource;
        if (baseSource instanceof MultiNoiseBiomeSource noiseBiomeSource && noiseBiomeSource.preset.isPresent()) {
            MultiNoiseBiomeSource.PresetInstance presetInstance = noiseBiomeSource.preset.get();
            modifiableSource = presetInstance.preset().biomeSource(presetInstance, false);
        } else {
            modifiableSource = baseSource;
        }

        Optional<JsonElement> sourceElement = BiomeSource.CODEC.encodeStart(dynOps, modifiableSource).result();
        if (sourceElement.isPresent()) {
            ResourceList resourceList = ConfigHandler.World.biomes.get(level.location().toString());
            JsonObject json = sourceElement.get().getAsJsonObject();
            JsonArray biomes = GsonHelper.getAsJsonArray(json, "biomes", new JsonArray());
            JsonArray newBiomes = new JsonArray();
            for (JsonElement biome : biomes) {
                ResourceLocation location = new ResourceLocation(GsonHelper.getAsString(biome.getAsJsonObject(), "biome"));
                if (resourceList.test(location)) {
                    newBiomes.add(biome);
                }
            }

            if (newBiomes.isEmpty()) {
                ForgeRegistries.BIOMES.getEntries().forEach(entry -> {
                    if (resourceList.test(entry.getKey().location())) {
                        JsonObject biomeEntry = new JsonObject();
                        JsonObject parameters = new JsonObject();
                        JsonArray emptyArray = new JsonArray();
                        emptyArray.add(0);
                        emptyArray.add(0);
                        parameters.addProperty("erosion", 0.0);
                        parameters.addProperty("depth", 0.0);
                        parameters.addProperty("weirdness", 0.0);
                        parameters.addProperty("offset", 0.0);
                        parameters.addProperty("temperature", 0.0);
                        parameters.addProperty("humidity", 0.0);
                        parameters.addProperty("continentalness", 0.0);
                        biomeEntry.add("parameters", parameters);
                        //noinspection ConstantConditions
                        biomeEntry.addProperty("biome", entry.getValue().getRegistryName().toString());

                        newBiomes.add(biomeEntry);
                    }
                });
            }

            json.add("biomes", newBiomes);

            Optional<Pair<BiomeSource, JsonElement>> newSourcePair = BiomeSource.CODEC.decode(dynOps, json).result();

            if (newSourcePair.isPresent()) {
                return newSourcePair.get().getFirst();
            }
        }

        return baseSource;
    }
}
