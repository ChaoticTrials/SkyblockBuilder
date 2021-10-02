package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.ModBlockTags;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import io.github.noeppi_noeppi.libx.data.provider.BlockTagProviderBase;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagProvider extends BlockTagProviderBase {

    public BlockTagProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(SkyblockBuilder.getInstance(), generator, helper);
    }

    @Override
    protected void setup() {
        this.getOrCreateBuilder(ModBlockTags.ADDITIONAL_VALID_SPAWN).addTag(BlockTags.LEAVES);
    }
}
