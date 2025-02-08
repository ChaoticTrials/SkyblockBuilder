package de.melanx.skyblockbuilder.registration;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import net.minecraft.world.item.Item;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "ITEM")
public class ModItems {

    public static final Item structureSaver = new ItemStructureSaver();
}
