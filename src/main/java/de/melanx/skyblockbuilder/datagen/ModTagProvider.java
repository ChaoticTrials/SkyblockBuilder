package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.ModBlockTags;
import io.github.noeppi_noeppi.libx.annotation.data.Datagen;
import io.github.noeppi_noeppi.libx.data.provider.CommonTagsProviderBase;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

@Datagen
public class ModTagProvider extends CommonTagsProviderBase {

    public ModTagProvider(ModX mod, DataGenerator generator, ExistingFileHelper helper) {
        super(mod, generator, helper);
    }

    @Override
    public void setup() {
        //noinspection unchecked
        this.block(ModBlockTags.ADDITIONAL_VALID_SPAWN).addTags(BlockTags.LEAVES);
    }
}
