package de.melanx.skyblockbuilder.world;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
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

    public static final Codec<SkyBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    CenterBiome.CODEC.listOf().fieldOf("center_biome").forGetter(biomeSource -> biomeSource.centerBiomes),
                    MultiNoiseBiomeSource.CODEC.fieldOf("parent").forGetter(biomeSource -> biomeSource.parent)
            )
            .apply(instance, SkyBiomeSource::new));

    public SkyBiomeSource(List<CenterBiome> centerBiomes, MultiNoiseBiomeSource parent) {
        super(Either.left(parent.parameters()));
        this.centerBiomes = centerBiomes;
        this.parent = parent;
    }

    @Nonnull
    @Override
    protected Codec<? extends BiomeSource> codec() {
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

        public static final Codec<CenterBiome> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Biome.CODEC.fieldOf("id").forGetter(CenterBiome::biome),
                        Codec.INT.fieldOf("radius").forGetter(CenterBiome::radius)
                )
                .apply(instance, CenterBiome::new));
    }
}
