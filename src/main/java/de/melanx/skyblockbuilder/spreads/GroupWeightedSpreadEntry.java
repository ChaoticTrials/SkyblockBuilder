package de.melanx.skyblockbuilder.spreads;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.util.SkyCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class GroupWeightedSpreadEntry implements WeightedSpread {

    public static final int DEFAULT_WEIGHT = 1;

    public static final Codec<GroupWeightedSpreadEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    SingleWeightedSpreadEntry.CODEC.listOf().fieldOf("entries").forGetter(GroupWeightedSpreadEntry::entries),
                    Codec.INT.optionalFieldOf("weight", DEFAULT_WEIGHT).forGetter(GroupWeightedSpreadEntry::weight),
                    Codec.INT.optionalFieldOf("amount").forGetter(GroupWeightedSpreadEntry::amount),
                    AutoSpread.CODEC.optionalFieldOf("auto_spread").forGetter(GroupWeightedSpreadEntry::autoSpread)
            ).apply(instance, GroupWeightedSpreadEntry::new));
    public static GroupWeightedSpreadEntry EMPTY = new GroupWeightedSpreadEntry(List.of(), DEFAULT_WEIGHT, Optional.empty(), Optional.empty());

    private final List<SingleWeightedSpreadEntry> entries;
    private final int weight;
    private final int amount;
    private final Optional<AutoSpread> autoSpread;

    public GroupWeightedSpreadEntry(List<SingleWeightedSpreadEntry> entries, int weight, Optional<Integer> amount, Optional<AutoSpread> autoSpread) {
        this.entries = entries;
        this.weight = weight;
        this.amount = amount.orElse(entries.size());
        this.autoSpread = autoSpread;
    }

    @Override
    public int weight() {
        return this.weight;
    }

    private List<SingleWeightedSpreadEntry> entries() {
        return List.copyOf(this.entries);
    }

    public Optional<AutoSpread> autoSpread() {
        return this.autoSpread;
    }

    public Optional<Integer> amount() {
        return this.amount == this.entries.size() ? Optional.empty() : Optional.of(this.amount);
    }

    public Set<SingleSpreadEntry> chooseEntries(RandomSource random) {
        if (this.amount > this.entries.size()) {
            throw new IllegalArgumentException("Requested amount exceeds the number of available entries.");
        }

        Set<SingleSpreadEntry> selectedEntries = new HashSet<>();
        List<Pair<SingleWeightedSpreadEntry, Integer>> weightedEntries = this.entries.stream()
                .map(entry -> Pair.of(entry, entry.weight()))
                .toList();

        if (this.amount().isEmpty()) {
            selectedEntries.addAll(this.entries.stream().map(SingleWeightedSpreadEntry::spread).toList());
        } else {
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
        }

        this.autoSpread.ifPresent(spread -> spread.apply(selectedEntries));

        return selectedEntries;
    }

    public record AutoSpread(Shape shape, int radius) {

        public static final Codec<AutoSpread> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Shape.CODEC.optionalFieldOf("shape", Shape.CIRCLE).forGetter(AutoSpread::shape),
                Codec.INT.fieldOf("radius").forGetter(AutoSpread::radius)
        ).apply(instance, AutoSpread::new));
        public static final AutoSpread DEFAULT = new AutoSpread(Shape.CIRCLE, 0);

        public void apply(Set<SingleSpreadEntry> entries) {
            int count = entries.size();
            if (count == 0) {
                return;
            }

            List<SingleSpreadEntry> entryList = new ArrayList<>(entries);
            entries.clear();

            List<BlockPos> positions;
            if (Objects.requireNonNull(this.shape) == Shape.CIRCLE) {
                positions = new ArrayList<>();
                double angleStep = 2 * Math.PI / count;
                for (int i = 0; i < count; i++) {
                    double angle = i * angleStep;
                    int x = (int) Math.round(this.radius * Math.cos(angle));
                    int z = (int) Math.round(this.radius * Math.sin(angle));
                    positions.add(new BlockPos(x, 0, z));
                }
            } else {
                positions = this.calculatePolygonPositions(this.shape.corners(), this.radius, count);
            }

            if (positions.size() > count) {
                positions = positions.subList(0, count);
            }

            Collections.shuffle(entryList);
            for (int i = 0; i < count; i++) {
                BlockPos offset = positions.get(i);
                entries.add(entryList.get(i).copyWithOffset(offset));
            }
        }

        private List<BlockPos> calculatePolygonPositions(int corners, int radius, int count) {
            List<BlockPos> positions = new ArrayList<>();
            // Compute vertices of the regular polygon.
            BlockPos[] vertices = new BlockPos[corners];
            for (int i = 0; i < corners; i++) {
                double angle = 2 * Math.PI * i / corners;
                int x = (int) Math.round(radius * Math.cos(angle));
                int z = (int) Math.round(radius * Math.sin(angle));
                vertices[i] = new BlockPos(x, 0, z);
            }

            if (count < corners) {
                // Distribute positions evenly among the polygon's corners.
                for (int j = 0; j < count; j++) {
                    int index = (int) Math.floor((j + 0.5) * corners / count);
                    positions.add(vertices[index % corners]);
                }
            } else {
                // Guarantee that all vertices (corners) appear.
                positions.add(vertices[0]);
                int extra = count - corners; // extra points to distribute along edges
                int baseExtra = extra / corners;
                int remainder = extra % corners;
                for (int i = 0; i < corners; i++) {
                    int extraForEdge = baseExtra + (i < remainder ? 1 : 0);
                    this.addEdgePoints(positions, vertices[i], vertices[(i + 1) % corners], extraForEdge);
                    positions.add(vertices[(i + 1) % corners]);
                }
            }

            return positions;
        }

        private void addEdgePoints(List<BlockPos> positions, BlockPos from, BlockPos to, int extraPoints) {
            // If no extra points are to be added, simply return.
            if (extraPoints <= 0) return;
            for (int i = 1; i <= extraPoints; i++) {
                double t = (double) i / (extraPoints + 1);
                int x = (int) Math.round(from.getX() + t * (to.getX() - from.getX()));
                int z = (int) Math.round(from.getZ() + t * (to.getZ() - from.getZ()));
                positions.add(new BlockPos(x, 0, z));
            }
        }


        public enum Shape {
            CIRCLE(0),
            SQUARE(4),
            HEXAGON(6);

            public static final Codec<Shape> CODEC = SkyCodecs.enumCodec(Shape.class);

            private final int corners;

            Shape(int corners) {
                this.corners = corners;
            }

            public int corners() {
                return this.corners;
            }
        }
    }
}
