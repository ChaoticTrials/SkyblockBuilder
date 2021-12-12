package de.melanx.skyblockbuilder.util;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class LazyBiomeRegistryWrapper extends MappedRegistry<Biome> {

    private static final HashMap<Registry<Biome>, LazyBiomeRegistryWrapper> cache = new HashMap<>();
    private final Registry<Biome> parent;
    private final Map<ResourceLocation, Biome> modifiedBiomes = new HashMap<>();

    private LazyBiomeRegistryWrapper(Registry<Biome> parent) {
        super(parent.key(), Lifecycle.experimental());
        this.parent = parent;
    }

    public static LazyBiomeRegistryWrapper get(Registry<Biome> parent) {
        if (parent instanceof LazyBiomeRegistryWrapper wrapper) {
            return wrapper;
        }

        return cache.computeIfAbsent(parent, LazyBiomeRegistryWrapper::new);
    }

    @Nonnull
    @Override
    public <V extends Biome> V registerMapping(int id, @Nonnull ResourceKey<Biome> key, @Nonnull V value, @Nonnull Lifecycle lifecycle) {
        throw new IllegalStateException("Can't register to biome registry wrapper.");
    }

    @Nonnull
    @Override
    public <R extends Biome> R register(@Nonnull ResourceKey<Biome> key, @Nonnull R value, @Nonnull Lifecycle lifecycle) {
        throw new IllegalStateException("Can't register to biome registry wrapper.");
    }

    @Nonnull
    @Override
    public <V extends Biome> V registerOrOverride(@Nonnull OptionalInt id, @Nonnull ResourceKey<Biome> key, @Nonnull V value, @Nonnull Lifecycle lifecycle) {
        throw new IllegalStateException("Can't register to biome registry wrapper.");
    }

    @Override
    @Nullable
    public Biome get(@Nullable ResourceLocation name) {
        return this.modified(this.parent.get(name));
    }

    @Nonnull
    @Override
    public Optional<Biome> getOptional(@Nullable ResourceLocation name) {
        return this.parent.getOptional(name).map(this::modified);
    }

    @Override
    @Nullable
    public Biome get(@Nullable ResourceKey<Biome> key) {
        return this.modified(this.parent.get(key));
    }

    @Override
    @Nullable
    public ResourceLocation getKey(@Nonnull Biome value) {
        return value.getRegistryName();
    }

    @Override
    public boolean containsKey(@Nonnull ResourceLocation name) {
        return this.parent.containsKey(name);
    }

    @Override
    public int getId(@Nullable Biome value) {
        return this.parent.getId(this.parent.get(value == null ? null : value.getRegistryName()));
    }

    @Override
    @Nullable
    public Biome byId(int value) {
        return this.modified(this.parent.byId(value));
    }

    @Nonnull
    @Override
    public Iterator<Biome> iterator() {
        Iterator<Biome> itr = this.parent.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public Biome next() {
                return itr.next();
            }
        };
    }

    @Nonnull
    @Override
    public Set<ResourceLocation> keySet() {
        return this.parent.keySet();
    }

    @Nonnull
    @Override
    public Set<Map.Entry<ResourceKey<Biome>, Biome>> entrySet() {
        return this.parent.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), this.modified(e.getValue())))
                .collect(Collectors.toSet());
    }

    private Biome modified(@Nullable Biome biome) {
        if (biome == null) {
            return null;
        } else if (this.modifiedBiomes.containsKey(biome.getRegistryName())) {
            return this.modifiedBiomes.get(biome.getRegistryName());
        } else {
            Biome modified = RandomUtility.modifyCopyBiome(biome);
            this.modifiedBiomes.put(biome.getRegistryName(), modified);
            return modified;
        }
    }
}
