package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.registration.ModBlockTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.neoforged.neoforge.common.Tags;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.tags.CommonTagsProviderBase;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ModTagProvider extends CommonTagsProviderBase {

    private final ModBiomeTagProvider modBiomeTagProvider;

    public ModTagProvider(DatagenContext context) {
        super(context);
        this.modBiomeTagProvider = new ModBiomeTagProvider(context.output(), CompletableFuture.completedFuture(context.registries().registryAccess()), context.mod().modid);
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        this.modBiomeTagProvider.run(cache);
        return super.run(cache);
    }

    @Override
    public void setup() {
        this.block(ModBlockTags.ADDITIONAL_VALID_SPAWN)
                .addTag(Tags.Blocks.GLASS_BLOCKS)
                .addTag(BlockTags.LEAVES)
                .add(Blocks.WATER);

        this.block(ModBlockTags.PREVENT_SCHEDULED_TICK)
                .addTag(BlockTags.SAND)
                .addTag(Tags.Blocks.GRAVELS);

        for (Block block : BuiltInRegistries.BLOCK.stream()
                .filter(block -> block instanceof Fallable)
                .toList()) {
            this.block(ModBlockTags.PREVENT_SCHEDULED_TICK)
                    .add(block);
        }
    }
}
