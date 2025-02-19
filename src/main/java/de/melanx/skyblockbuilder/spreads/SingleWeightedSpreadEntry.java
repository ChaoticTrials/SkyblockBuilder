package de.melanx.skyblockbuilder.spreads;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SingleWeightedSpreadEntry(SingleSpreadEntry spread, int weight) implements WeightedSpread {

    public static final Codec<SingleWeightedSpreadEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    SingleSpreadEntry.CODEC.fieldOf("spread").forGetter(SingleWeightedSpreadEntry::spread),
                    Codec.INT.fieldOf("weight").forGetter(SingleWeightedSpreadEntry::weight)
            ).apply(instance, SingleWeightedSpreadEntry::new));

}
