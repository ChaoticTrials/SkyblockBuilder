package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.ModBlockTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
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
    }
}
