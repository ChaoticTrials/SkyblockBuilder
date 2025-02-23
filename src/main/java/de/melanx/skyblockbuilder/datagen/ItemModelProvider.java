package de.melanx.skyblockbuilder.datagen;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
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
    protected void defaultItem(ResourceLocation id, Item item) {
        ItemModelBuilder builder = this.withExistingParent(id.getPath(), ItemModelProviderBase.GENERATED);
        for (int i = 0; i < 3; i++) {
            String name = id.getPath() + (i == 0 ? "" : String.format("_%02d", i));
            builder.override().predicate(SkyblockBuilder.getInstance().resource("structure_saver_type"), i).model(
                    this.withExistingParent(name, ItemModelProviderBase.GENERATED)
                            .texture("layer0", ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/" + name))
            ).end();
        }
    }
}
