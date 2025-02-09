package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.registration.ModBlocks;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.model.BlockStateProviderBase;

public class BlockStatesProvider extends BlockStateProviderBase {

    public BlockStatesProvider(DatagenContext context) {
        super(context);
    }

    @Override
    protected void setup() {
        this.manualModel(ModBlocks.spawnBlock);
    }
}
