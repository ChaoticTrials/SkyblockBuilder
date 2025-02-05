package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.ModBlockTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.tags.CommonTagsProviderBase;

public class ModTagProvider extends CommonTagsProviderBase {

    public ModTagProvider(DatagenContext context) {
        super(context);
    }

    @Override
    public void setup() {
        //noinspection unchecked
        this.block(ModBlockTags.ADDITIONAL_VALID_SPAWN)
                .addTags(BlockTags.LEAVES)
                .add(Blocks.WATER);

        //noinspection unchecked
        this.block(ModBlockTags.PREVENT_SCHEDULED_TICK)
                .addTags(BlockTags.SAND);

        for (Block block : BuiltInRegistries.BLOCK.stream()
                .filter(block -> block instanceof Fallable)
                .toList()) {
            this.block(ModBlockTags.PREVENT_SCHEDULED_TICK)
                    .add(block);
        }
    }
}
