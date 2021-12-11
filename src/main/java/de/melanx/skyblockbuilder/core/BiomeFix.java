package de.melanx.skyblockbuilder.core;

import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.core.IdMap;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;

public class BiomeFix {

    public static BiomeGenerationSettingsBuilder rebuildSettingsBuilder(BiomeGenerationSettingsBuilder builder) {
        return new BiomeGenerationSettingsBuilder(RandomUtility.modifyBiomeGenerationSettings(builder.build()));
    }

    /**
     * Patched into {@link ChunkBiomeContainer#writeBiomes()} redirecting the call to
     * {@link IdMap#getId(Object)} passing the {@link IdMap} reference and all arguments.
     */
    public static int getId(IdMap<Biome> lookup, Object value) {
        if (value instanceof Biome biome) {
            int id = lookup.getId(biome);
            if (id >= 0) {
                return id;
            } else {
                return RandomUtility.validateBiome(biome);
            }
        } else {
            return 0;
        }
    }
}
