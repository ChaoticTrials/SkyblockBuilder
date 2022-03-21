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

import java.util.Optional;

public class BiomeSourceConverter {

    public static BiomeSource customBiomeSource(ResourceKey<Level> level, RegistryAccess registryAccess, BiomeSource source) {
        RegistryOps<JsonElement> dynOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        if (source instanceof MultiNoiseBiomeSource noiseBiomeSource) {
            if (noiseBiomeSource.preset.isPresent()) {
                MultiNoiseBiomeSource.PresetInstance presetInstance = noiseBiomeSource.preset.get();
                source = presetInstance.preset().biomeSource(presetInstance, false);
            }
        }

        Optional<JsonElement> sourceElement = BiomeSource.CODEC.encodeStart(dynOps, source).result();
        if (sourceElement.isPresent()) {
            JsonObject json = sourceElement.get().getAsJsonObject();
            JsonArray biomes = GsonHelper.getAsJsonArray(json, "biomes", new JsonArray());
            JsonArray newBiomes = new JsonArray();
            for (JsonElement biome : biomes) {
                ResourceLocation location = new ResourceLocation(GsonHelper.getAsString(biome.getAsJsonObject(), "biome"));
                ResourceList resourceList = ConfigHandler.World.biomes.get(level.location().toString());
                if (resourceList.test(location)) {
                    newBiomes.add(biome);
                }
            }

            json.add("biomes", newBiomes);

            Optional<Pair<BiomeSource, JsonElement>> newSourcePair = BiomeSource.CODEC.decode(dynOps, json).result();

            if (newSourcePair.isPresent()) {
                return newSourcePair.get().getFirst();
            }
        }

        return source;
    }
}
