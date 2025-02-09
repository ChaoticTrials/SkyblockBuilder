package de.melanx.skyblockbuilder.registration;

import com.mojang.serialization.MapCodec;
import de.melanx.skyblockbuilder.world.SkyBiomeSource;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "BIOME_SOURCE")
public class ModBiomeSourceCodecs {

    public static final MapCodec<SkyBiomeSource> sky = SkyBiomeSource.CODEC;
}
