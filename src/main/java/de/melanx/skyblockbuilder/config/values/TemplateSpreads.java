package de.melanx.skyblockbuilder.config.values;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import de.melanx.skyblockbuilder.spreads.GroupWeightedSpreadEntry;
import de.melanx.skyblockbuilder.spreads.SingleSpreadEntry;

import java.util.List;

public record TemplateSpreads(List<Either<SingleSpreadEntry, GroupWeightedSpreadEntry>> spreads) {

    public static final Codec<TemplateSpreads> CODEC = Codec.either(SingleSpreadEntry.CODEC, GroupWeightedSpreadEntry.CODEC).listOf()
            .xmap(TemplateSpreads::new, TemplateSpreads::spreads);
    public static final TemplateSpreads EMPTY = new TemplateSpreads(List.of());
}
