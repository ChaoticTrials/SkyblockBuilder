package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import io.github.noeppi_noeppi.libx.data.provider.ItemModelProviderBase;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelProvider extends ItemModelProviderBase {

    public ItemModelProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(SkyblockBuilder.getInstance(), generator, helper);
    }

    @Override
    protected void setup() {

    }
}
