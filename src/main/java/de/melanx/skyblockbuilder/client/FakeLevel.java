package de.melanx.skyblockbuilder.client;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FakeLevel extends ClientLevel {

    private static FakeLevel instance;

    public FakeLevel() {
        super(FakeLevel.fakeClientPacketListener(),
                new ClientLevelData(Difficulty.EASY, false, true),
                Level.OVERWORLD,
                new FakeHolder<>(FakeLevel.fakeDimensionType()),
                0, 0, () -> null,
                new LevelRenderer(Minecraft.getInstance(),
                        Minecraft.getInstance().getEntityRenderDispatcher(),
                        Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                        Minecraft.getInstance().renderBuffers()),
                false, 0);
    }

    public static FakeLevel getInstance() {
        if (instance == null) {
            instance = new FakeLevel();
        }

        return instance;
    }

    private static ClientPacketListener fakeClientPacketListener() {
        //noinspection DataFlowIssue
        return new ClientPacketListener(Minecraft.getInstance(), null, new Connection(PacketFlow.CLIENTBOUND), null, null, null) {

            @Nonnull
            @Override
            public RegistryAccess registryAccess() {
                return new FakeRegistry();
            }
        };
    }

    private static DimensionType fakeDimensionType() {
        return new DimensionType(OptionalLong.empty(), true, false, false, false, 1.0, false, false, 0, 256, 256, BlockTags.INFINIBURN_OVERWORLD, BuiltinDimensionTypes.OVERWORLD_EFFECTS, 1,
                new DimensionType.MonsterSettings(false, false, ConstantInt.ZERO, 0));
    }

    @SuppressWarnings("NullableProblems")
    private static class FakeRegistry implements RegistryAccess {

        @Nonnull
        @Override
        public <E> Optional<Registry<E>> registry(@Nonnull ResourceKey<? extends Registry<? extends E>> registryKey) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(@Nonnull ResourceKey<? extends Registry<? extends T>> registryKey) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public <E> Registry<E> registryOrThrow(@Nonnull ResourceKey<? extends Registry<? extends E>> registryKey) {
            return new FakeRegistryObject<>();
        }

        @Override
        public Stream<RegistryEntry<?>> registries() {
            return null;
        }

        @Override
        public Frozen freeze() {
            return null;
        }

        @Override
        public Lifecycle allRegistriesLifecycle() {
            return null;
        }
    }

    @SuppressWarnings("NullableProblems")
    private static class FakeRegistryObject<T> implements Registry<T> {

        @Override
        public Holder.Reference<T> getHolderOrThrow(ResourceKey<T> key) {
            return null;
        }

        @Override
        public ResourceKey<? extends Registry<T>> key() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getKey(T value) {
            return null;
        }

        @Override
        public Optional<ResourceKey<T>> getResourceKey(T value) {
            return Optional.empty();
        }

        @Override
        public int getId(@Nullable T value) {
            return 0;
        }

        @Nullable
        @Override
        public T byId(int id) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Nullable
        @Override
        public T get(@Nullable ResourceKey<T> key) {
            return null;
        }

        @Nullable
        @Override
        public T get(@Nullable ResourceLocation name) {
            return null;
        }

        @Override
        public Lifecycle lifecycle(T value) {
            return null;
        }

        @Override
        public Lifecycle registryLifecycle() {
            return null;
        }

        @Override
        public Set<ResourceLocation> keySet() {
            return null;
        }

        @Override
        public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
            return null;
        }

        @Override
        public Set<ResourceKey<T>> registryKeySet() {
            return null;
        }

        @Override
        public Optional<Holder.Reference<T>> getRandom(RandomSource random) {
            return Optional.empty();
        }

        @Override
        public boolean containsKey(ResourceLocation name) {
            return false;
        }

        @Override
        public boolean containsKey(ResourceKey<T> key) {
            return false;
        }

        @Override
        public Registry<T> freeze() {
            return null;
        }

        @Override
        public Holder.Reference<T> createIntrusiveHolder(T value) {
            return null;
        }

        @Override
        public Optional<Holder.Reference<T>> getHolder(int id) {
            return Optional.empty();
        }

        @Override
        public Optional<Holder.Reference<T>> getHolder(ResourceKey<T> key) {
            return Optional.empty();
        }

        @Override
        public Holder<T> wrapAsHolder(T value) {
            return null;
        }

        @Override
        public Stream<Holder.Reference<T>> holders() {
            return null;
        }

        @Override
        public Optional<HolderSet.Named<T>> getTag(TagKey<T> key) {
            return Optional.empty();
        }

        @Override
        public HolderSet.Named<T> getOrCreateTag(TagKey<T> key) {
            return null;
        }

        @Override
        public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
            return null;
        }

        @Override
        public Stream<TagKey<T>> getTagNames() {
            return null;
        }

        @Override
        public void resetTags() {

        }

        @Override
        public void bindTags(Map<TagKey<T>, List<Holder<T>>> tagMap) {

        }

        @Override
        public HolderOwner<T> holderOwner() {
            return null;
        }

        @Override
        public HolderLookup.RegistryLookup<T> asLookup() {
            return null;
        }

        @Override
        public Iterator<T> iterator() {
            return null;
        }
    }

    public record FakeHolder<T>(T value) implements Holder<T> {

        @Override
        public boolean isBound() {
            return false;
        }

        @Override
        public boolean is(@Nonnull ResourceLocation location) {
            return false;
        }

        @Override
        public boolean is(@Nonnull ResourceKey<T> resourceKey) {
            return false;
        }

        @Override
        public boolean is(@Nonnull Predicate<ResourceKey<T>> predicate) {
            return false;
        }

        @Override
        public boolean is(@Nonnull TagKey<T> tagKey) {
            return false;
        }

        @Nonnull
        @Override
        public Stream<TagKey<T>> tags() {
            return Stream.of();
        }

        @Nonnull
        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return Either.right(this.value);
        }

        @Nonnull
        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            //noinspection rawtypes
            Constructor<ResourceKey> constructor = ObfuscationReflectionHelper.findConstructor(ResourceKey.class, ResourceLocation.class, ResourceLocation.class);
            constructor.setAccessible(true);
            try {
                return Optional.of(constructor.newInstance(new ResourceLocation(""), new ResourceLocation("")));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return Optional.empty();
        }

        @Nonnull
        @Override
        public Kind kind() {
            return Kind.DIRECT;
        }

        @Override
        public boolean canSerializeIn(@Nonnull HolderOwner<T> owner) {
            return false;
        }
    }
}
