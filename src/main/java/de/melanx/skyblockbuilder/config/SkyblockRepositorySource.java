//package de.melanx.skyblockbuilder.config;
//
//import de.melanx.skyblockbuilder.SkyblockBuilder;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.packs.FilePackResources;
//import net.minecraft.server.packs.PackResources;
//import net.minecraft.server.packs.PackType;
//import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
//import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
//import net.minecraft.server.packs.repository.Pack;
//import net.minecraft.server.packs.repository.PackSource;
//import net.minecraft.server.packs.repository.RepositorySource;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.function.Consumer;
//import java.util.function.Predicate;
//
//public class SkyblockRepositorySource implements RepositorySource {
//
//    @Override
//    public void loadPacks(@Nonnull Consumer<Pack> packConsumer, @Nonnull Pack.PackConstructor constructor) {
//        Pack.create("packName", true, () -> new VirtualPack(), constructor, Pack.Position.TOP, PackSource.BUILT_IN);
//        packConsumer.accept(constructor.create("das", Component.literal(""), false, () -> null, new PackMetadataSection(Component.literal(""), 10), Pack.Position.TOP, PackSource.WORLD, false));
//    }
//
//    private static class VirtualPack implements PackResources {
//
//        @Nullable
//        @Override
//        public InputStream getRootResource(@Nonnull String name) {
//            return null;
//        }
//
//        @Nonnull
//        @Override
//        public InputStream getResource(@Nonnull PackType p_10289_, @Nonnull ResourceLocation p_10290_) {
//            this.getResourceAsStream(p_10289_, p_10290_);
//            return null;
//        }
//
//        @Nonnull
//        @Override
//        public Collection<ResourceLocation> getResources(@Nonnull PackType type, @Nonnull String namespace, @Nonnull String s, @Nonnull Predicate<ResourceLocation> predicate) {
//            Set<ResourceLocation> set = new HashSet<>();
//            if (namespace.equals(SkyblockBuilder.getInstance().modid)) {
//                set.add()
//            }
//            return Set.of(new ResourceLocation("skyblockbuilder:worldgen/world_preset/skylands.json"));
//        }
//
//        @Override
//        public boolean hasResource(@Nonnull PackType packType, @Nonnull ResourceLocation location) {
//            return location.equals(SkyblockBuilder.getInstance().resource("worldgen/world_preset/skylands.json"));
//        }
//
//        @Nonnull
//        @Override
//        public Set<String> getNamespaces(@Nonnull PackType packType) {
//            return Set.of(SkyblockBuilder.getInstance().modid);
//        }
//
//        @Nullable
//        @Override
//        public <T> T getMetadataSection(@Nonnull MetadataSectionSerializer<T> serializer) throws IOException {
//            return null;
//        }
//
//        @Nonnull
//        @Override
//        public String getName() {
//            return "KEKW";
//        }
//
//        @Override
//        public void close() {
//
//        }
//    }
//}
