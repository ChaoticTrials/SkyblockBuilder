package de.melanx.skyblockbuilder.util;

import com.mojang.serialization.Codec;

import java.util.Locale;

public class SkyCodecs {

    public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> clazz) {
        return Codec.STRING.xmap(
                value -> Enum.valueOf(clazz, value.toUpperCase(Locale.ROOT)),
                s -> s.name().toLowerCase(Locale.ROOT)
        );
    }
}
