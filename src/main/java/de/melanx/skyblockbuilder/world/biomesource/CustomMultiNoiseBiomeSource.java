package de.melanx.skyblockbuilder.world.biomesource;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import de.melanx.skyblockbuilder.datagen.ModBiomeTagProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;

import javax.annotation.Nonnull;
import java.util.List;

// This class is intended to use with overworld only!
public class CustomMultiNoiseBiomeSource extends MultiNoiseBiomeSource {

    public static final MapCodec<CustomMultiNoiseBiomeSource> CODEC = Codec.mapEither(DIRECT_CODEC, MultiNoiseBiomeSourceParameterList.CODEC
                    .fieldOf("preset")
                    .withLifecycle(Lifecycle.stable()))
            .xmap(CustomMultiNoiseBiomeSource::new, biomeSource -> biomeSource.parameters);
    private Climate.ParameterList<Holder<Biome>> modifiedList;

    public CustomMultiNoiseBiomeSource(Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters) {
        this(parameters.right().orElseThrow(() -> new IllegalArgumentException("Parameter list must be present")));
    }

    public CustomMultiNoiseBiomeSource(Holder<MultiNoiseBiomeSourceParameterList> parameters) {
        super(Either.right(parameters));
    }

    @Nonnull
    @Override
    public Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters.map(parameterList -> parameterList, parameterListHolder -> {
            if (this.modifiedList == null) {
                List<Pair<Climate.ParameterPoint, Holder<Biome>>> list = parameterListHolder.value().parameters().values()
                        .stream()
                        .filter(pair -> pair.getSecond().is(ModBiomeTagProvider.IS_OVERWORLD))
                        .toList();
                this.modifiedList = new Climate.ParameterList<>(list);
            }

            return this.modifiedList;
        });
    }

    @Nonnull
    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }
}
