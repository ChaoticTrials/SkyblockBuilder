package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.WorldPresetTags;
import org.moddingx.libx.datagen.DatagenContext;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class TagsProvider extends WorldPresetTagsProvider {

    public TagsProvider(DatagenContext context) {
        super(context.output(), CompletableFuture.completedFuture(context.registries().registryAccess()), context.mod().modid, context.fileHelper());
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {
        this.tag(WorldPresetTags.NORMAL).add(ResourceKey.create(Registries.WORLD_PRESET, SkyblockBuilder.getInstance().resource("skyblock")));
    }
}
