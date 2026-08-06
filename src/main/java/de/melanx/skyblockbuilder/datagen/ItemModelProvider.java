package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.item.StructureSaverSettings;
import de.melanx.skyblockbuilder.registration.ModDataComponentTypes;
import de.melanx.skyblockbuilder.registration.ModItems;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.model.ItemModelProviderBase;

public class ItemModelProvider extends ItemModelProviderBase {

    public ItemModelProvider(DatagenContext context) {
        super(context);
    }

    @Override
    protected void setup() {
        // NO-OP
    }

    @Override
    protected void defaultItem(Item item, ItemModelGenerators itemModels) {
        if (item != ModItems.structureSaver) {
            super.defaultItem(item, itemModels);
            return;
        }

        Identifier island = itemModels.createFlatItemModel(item, ModelTemplates.FLAT_ITEM);
        Identifier spread = itemModels.createFlatItemModel(item, "_01", ModelTemplates.FLAT_ITEM);
        Identifier nether = itemModels.createFlatItemModel(item, "_02", ModelTemplates.FLAT_ITEM);

        itemModels.itemModelOutput.accept(item, ItemModelUtils.select(
                new ComponentContents<>(ModDataComponentTypes.structureSaverType),
                ItemModelUtils.plainModel(island),
                ItemModelUtils.when(StructureSaverSettings.Type.ISLAND, ItemModelUtils.plainModel(island)),
                ItemModelUtils.when(StructureSaverSettings.Type.SPREAD, ItemModelUtils.plainModel(spread)),
                ItemModelUtils.when(StructureSaverSettings.Type.NETHER, ItemModelUtils.plainModel(nether))
        ));
    }
}
