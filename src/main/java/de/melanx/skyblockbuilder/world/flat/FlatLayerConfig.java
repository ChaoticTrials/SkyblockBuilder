package de.melanx.skyblockbuilder.world.flat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FlatLayerConfig {

    public static final Codec<FlatLayerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").orElse(Blocks.AIR).forGetter(inst -> inst.getBlockState().getBlock()),
            Codec.INT.optionalFieldOf("height", 1).forGetter(FlatLayerConfig::getHeight),
            Extras.CODEC.optionalFieldOf("extras", Extras.EMPTY).forGetter(inst -> inst.extras)
    ).apply(instance, FlatLayerConfig::new));

    private final Block block;
    private final int height;
    private final Extras extras;
    private WeightedRandomList<WeightedBlockEntry> weightedBlockEntries;

    public FlatLayerConfig(Block block) {
        this(block, 1);
    }

    public FlatLayerConfig(Block block, int height) {
        this(block, height, Extras.EMPTY);
    }

    public FlatLayerConfig(Block block, int height, Extras extras) {
        this.block = block;
        this.height = height;
        this.extras = extras;
    }

    public BlockState getBlockState() {
        return this.block.defaultBlockState();
    }

    public int getHeight() {
        return this.height;
    }

    public boolean hasExtra() {
        return !this.extras.isEmpty();
    }

    public boolean checkChance(RandomSource random) {
        return random.nextDouble() < this.extras.chance;
    }

    public WeightedRandomList<WeightedBlockEntry> getExtraBlocks() {
        if (this.weightedBlockEntries == null) {
            this.weightedBlockEntries = WeightedRandomList.create(this.extras.extraBlocks());
        }

        return this.weightedBlockEntries;
    }

    // [Vanilla copy]
    @Nullable
    public static FlatLayerConfig getLayerConfig(String setting, int currentLayers) {
        String[] info = setting.split("\\*", 2);
        int i;
        if (info.length == 2) {
            try {
                i = Math.max(Integer.parseInt(info[0]), 0);
            } catch (NumberFormatException numberformatexception) {
                SkyblockBuilder.getLogger().error("Error while parsing surface settings string => {}", numberformatexception.getMessage());
                return null;
            }
        } else {
            i = 1;
        }

        int maxLayers = Math.min(currentLayers + i, 384);
        int height = maxLayers - currentLayers;
        String blockName = info[info.length - 1];

        Block block;
        ResourceLocation blockId = ResourceLocation.tryParse(blockName);
        try {
            block = BuiltInRegistries.BLOCK.get(blockId);
        } catch (Exception exception) {
            SkyblockBuilder.getLogger().error("Error while parsing surface settings string => {}", exception.getMessage());
            return null;
        }

        if (block == Blocks.AIR && !BuiltInRegistries.BLOCK.getKey(Blocks.AIR).equals(blockId)) {
            SkyblockBuilder.getLogger().error("Error while parsing surface settings string => Unknown block, {}", blockName);
        }

        return new FlatLayerConfig(block, height);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.height > 1) {
            sb.append(this.height);
            sb.append("*");
        }

        sb.append(BuiltInRegistries.BLOCK.getKey(this.block));

        return sb.toString();
    }

    public record Extras(List<WeightedBlockEntry> extraBlocks, double chance) {

        public static final Extras EMPTY = new Extras(List.of(), 0.0D);
        public static final Codec<Extras> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                WeightedBlockEntry.CODEC.listOf().fieldOf("blocks").forGetter(Extras::extraBlocks),
                Codec.DOUBLE.fieldOf("chance").forGetter(Extras::chance)
        ).apply(instance, Extras::new));

        public boolean isEmpty() {
            return this == EMPTY || this.extraBlocks.isEmpty();
        }
    }

    public record WeightedBlockEntry(Block block, int weight) implements WeightedEntry {

        public static final Codec<WeightedBlockEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").orElse(Blocks.AIR).forGetter(WeightedBlockEntry::block),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(WeightedBlockEntry::weight)
        ).apply(instance, WeightedBlockEntry::new));

        @Nonnull
        @Override
        public Weight getWeight() {
            return Weight.of(this.weight);
        }
    }
}
