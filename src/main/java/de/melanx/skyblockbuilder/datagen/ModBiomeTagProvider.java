package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ModBiomeTagProvider extends BiomeTagsProvider {

    public static final TagKey<Biome> IS_OVERWORLD = TagKey.create(Registries.BIOME, SkyblockBuilder.getInstance().resource("is_overworld"));

    public ModBiomeTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, String modid, ExistingFileHelper fileHelper) {
        super(packOutput, lookupProvider, modid, fileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {
        this.tag(IS_OVERWORLD).addTag(Tags.Biomes.IS_OVERWORLD);
    }

    @Nonnull
    @Override
    public String getName() {
        return this.modId + " biome tags";
    }
}
