package de.melanx.skyblockbuilder.spreads;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

public record SingleSpreadEntry(String file, BlockPos minOffset, BlockPos maxOffset, Origin origin) implements SpreadInfo {

    private SingleSpreadEntry(String file, MinMax offset, Origin origin) {
        this(file, offset.min, offset.max, origin);
    }

    public static final Codec<SingleSpreadEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("file").forGetter(SingleSpreadEntry::file),
                    Codec.either(BlockPos.CODEC, MinMax.CODEC).optionalFieldOf("offset", Either.left(BlockPos.ZERO)).xmap(
                            either -> either.map(
                                    blockPos -> new MinMax(blockPos, blockPos),
                                    minMax -> minMax
                            ), minMax -> minMax.min.equals(minMax.max) ? Either.left(minMax.min) : Either.right(minMax)
                    ).forGetter(SingleSpreadEntry::offset),
                    Origin.CODEC.optionalFieldOf("origin", Origin.CENTER).forGetter(SingleSpreadEntry::origin)
            ).apply(instance, SingleSpreadEntry::new));

    public static final SingleSpreadEntry DEFAULT = new SingleSpreadEntry("default.nbt", BlockPos.ZERO, BlockPos.ZERO, SpreadInfo.Origin.ZERO);

    public SingleSpreadEntry copyWithOffset(BlockPos offset) {
        return new SingleSpreadEntry(this.file, offset, offset, this.origin);
    }

    private MinMax offset() {
        return new MinMax(this.minOffset, this.maxOffset);
    }

    private record MinMax(@Nonnull BlockPos min, @Nonnull BlockPos max) {
        public static final Codec<MinMax> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("min").forGetter(MinMax::min),
                BlockPos.CODEC.fieldOf("max").forGetter(MinMax::max)
        ).apply(instance, MinMax::new));
    }
}
