package de.melanx.skyblockbuilder.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import javax.annotation.Nonnull;
import java.util.List;

public class SkyBiomeSource extends MultiNoiseBiomeSource {

    private final List<CenterBiome> centerBiomes;
    private final MultiNoiseBiomeSource parent;

    public static final MapCodec<SkyBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.list(CenterBiome.CODEC.codec()).fieldOf("center_biome").forGetter(lol -> lol.centerBiomes),
                    MultiNoiseBiomeSource.CODEC.fieldOf("parent").forGetter(lol -> lol.parent)
            )
            .apply(instance, SkyBiomeSource::new));

    public SkyBiomeSource(List<CenterBiome> centerBiomes, MultiNoiseBiomeSource parent) {
        super(parent.parameters);
        this.centerBiomes = centerBiomes;
        this.parent = parent;
    }

    @Nonnull
    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Nonnull
    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, @Nonnull Climate.Sampler sampler) {
        int radius = 0;

        int blockX = this.calculateCenteredBlockPos(QuartPos.toBlock(x));
        int blockZ = this.calculateCenteredBlockPos(QuartPos.toBlock(z));

        for (CenterBiome centerBiome : this.centerBiomes) {
            radius += centerBiome.radius();

            if (blockX * blockX + blockZ * blockZ < radius * radius) {
                return centerBiome.biome();
            }
        }

        return super.getNoiseBiome(x, y, z, sampler);
    }

    private int calculateCenteredBlockPos(int i) {
        int div = WorldConfig.islandDistance;

        int blockPos = (((i - TemplatesConfig.defaultOffset) % div) + div) % div;
        if (blockPos > div / 2) {
            blockPos -= div;
        }

        return blockPos;
    }

    public record CenterBiome(Holder<Biome> biome, int radius) {

        public static final MapCodec<CenterBiome> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Biome.CODEC.fieldOf("id").forGetter(CenterBiome::biome),
                        Codec.INT.fieldOf("radius").forGetter(CenterBiome::radius)
                )
                .apply(instance, CenterBiome::new));
    }
}
