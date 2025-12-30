package de.melanx.skyblockbuilder.registration;

import com.mojang.serialization.MapCodec;
import de.melanx.skyblockbuilder.world.biomesource.CustomMultiNoiseBiomeSource;
import de.melanx.skyblockbuilder.world.biomesource.SkyBiomeSource;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "BIOME_SOURCE")
public class ModBiomeSourceCodecs {

    public static final MapCodec<SkyBiomeSource> sky = SkyBiomeSource.CODEC;
    public static final MapCodec<CustomMultiNoiseBiomeSource> skyOverworld = CustomMultiNoiseBiomeSource.CODEC;
}
