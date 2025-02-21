package de.melanx.skyblockbuilder.spreads;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record SingleWeightedSpreadEntry(SingleSpreadEntry spread, int weight) implements WeightedSpread {

    public static final Codec<SingleWeightedSpreadEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.either(Codec.STRING, SingleSpreadEntry.CODEC).xmap(
                            either -> either.map(
                                    fileName -> new SingleSpreadEntry(fileName, BlockPos.ZERO, BlockPos.ZERO, SpreadInfo.Origin.CENTER),
                                    entry -> entry
                            ), Either::right
                    ).fieldOf("spread").forGetter(SingleWeightedSpreadEntry::spread),
                    Codec.INT.fieldOf("weight").forGetter(SingleWeightedSpreadEntry::weight)
            ).apply(instance, SingleWeightedSpreadEntry::new));
}
