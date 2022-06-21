package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import net.minecraft.world.item.Item;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "ITEMS")
public class ModItems {

    public static final Item structureSaver = new ItemStructureSaver();
}
