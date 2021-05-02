package de.melanx.skyblockbuilder.util;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class LazyBiomeRegistryWrapper extends SimpleRegistry<Biome> {

    private final Registry<Biome> parent;
    private final Map<ResourceLocation, Biome> modifiedBiomes = new HashMap<>();

    public LazyBiomeRegistryWrapper(Registry<Biome> parent) {
        super(parent.getRegistryKey(), Lifecycle.experimental());
        this.parent = parent;

    }

    @Nonnull
    @Override
    public <V extends Biome> V register(int id, @Nonnull RegistryKey<Biome> key, @Nonnull V value, @Nonnull Lifecycle lifecycle) {
        throw new IllegalStateException("Can't register to biome registry wrapper.");
    }

    @Nonnull
    @Override
    public <R extends Biome> R register(@Nonnull RegistryKey<Biome> key, @Nonnull R value, @Nonnull Lifecycle lifecycle) {
        throw new IllegalStateException("Can't register to biome registry wrapper.");
    }

    @Nonnull
    @Override
    public <V extends Biome> V validateAndRegister(@Nonnull OptionalInt id, @Nonnull RegistryKey<Biome> key, @Nonnull V value, @Nonnull Lifecycle lifecycle) {
        throw new IllegalStateException("Can't register to biome registry wrapper.");
    }

    @Override
    @Nullable
    public Biome getOrDefault(@Nullable ResourceLocation name) {
        return this.modified(this.parent.getOrDefault(name));
    }

    @Nonnull
    @Override
    public Optional<Biome> getOptional(@Nullable ResourceLocation name) {
        return this.parent.getOptional(name).map(this::modified);
    }

    @Override
    @Nullable
    public Biome getValueForKey(@Nullable RegistryKey<Biome> name) {
        return this.modified(this.parent.getValueForKey(name));
    }

    @Override
    @Nullable
    public ResourceLocation getKey(@Nonnull Biome value) {
        return value.getRegistryName();
    }

    @Override
    public boolean containsKey(@Nonnull ResourceLocation key) {
        return this.parent.containsKey(key);
    }

    @Override
    public int getId(@Nullable Biome value) {
        return this.parent.getId(this.parent.getOrDefault(value == null ? null : value.getRegistryName()));
    }

    @Override
    @Nullable
    public Biome getByValue(int id) {
        return this.modified(this.parent.getByValue(id));
    }

    @Nonnull
    @Override
    public Iterator<Biome> iterator() {
        Iterator<Biome> itr = this.parent.iterator();
        return new Iterator<Biome>() {
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
    public Set<Map.Entry<RegistryKey<Biome>, Biome>> getEntries() {
        return this.parent.getEntries().stream()
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
