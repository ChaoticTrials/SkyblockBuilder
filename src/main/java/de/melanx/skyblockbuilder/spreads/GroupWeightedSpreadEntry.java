package de.melanx.skyblockbuilder.spreads;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupWeightedSpreadEntry implements WeightedSpread {

    public static final Codec<GroupWeightedSpreadEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    SingleWeightedSpreadEntry.CODEC.listOf().fieldOf("entries").forGetter(GroupWeightedSpreadEntry::entries),
                    Codec.INT.optionalFieldOf("weight", 1).forGetter(GroupWeightedSpreadEntry::weight),
                    Codec.INT.optionalFieldOf("amount", 1).forGetter(GroupWeightedSpreadEntry::amount)
            ).apply(instance, GroupWeightedSpreadEntry::new));
    public static GroupWeightedSpreadEntry EMPTY = new GroupWeightedSpreadEntry(List.of(), 1, 1);

    private final List<SingleWeightedSpreadEntry> entries;
    private final int weight;
    private final int amount;

    public GroupWeightedSpreadEntry(List<SingleWeightedSpreadEntry> entries, int weight, int amount) {
        this.entries = entries;
        this.weight = weight;
        this.amount = amount;
    }

    @Override
    public int weight() {
        return this.weight;
    }

    private List<SingleWeightedSpreadEntry> entries() {
        return List.copyOf(this.entries);
    }

    public int amount() {
        return this.amount;
    }

    public Set<SingleSpreadEntry> chooseEntries(RandomSource random) {
        if (this.amount > this.entries.size()) {
            throw new IllegalArgumentException("Requested amount exceeds the number of available entries.");
        }

        Set<SingleSpreadEntry> selectedEntries = new HashSet<>();
        List<Pair<SingleWeightedSpreadEntry, Integer>> weightedEntries = this.entries.stream()
                .map(entry -> Pair.of(entry, entry.weight()))
                .toList();

        int totalWeight = weightedEntries.stream().mapToInt(Pair::getRight).sum();

        while (selectedEntries.size() < this.amount) {
            int rand = random.nextInt(totalWeight);
            int cumulativeWeight = 0;

            for (Pair<SingleWeightedSpreadEntry, Integer> pair : weightedEntries) {
                cumulativeWeight += pair.getRight();
                if (rand < cumulativeWeight && !selectedEntries.contains(pair.getLeft().spread())) {
                    selectedEntries.add(pair.getLeft().spread());
                    break;
                }
            }
        }

        return selectedEntries;
    }
}
