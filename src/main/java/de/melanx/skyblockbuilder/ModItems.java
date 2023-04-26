package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "ITEMS")
public class ModItems {

    public static final Item structureSaver = new ItemStructureSaver();

    public static void registerTab(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(SkyblockBuilder.getInstance().resource("tab"), builder -> {
            builder.title(Component.literal("Skyblock Builder"));
            builder.icon(() -> new ItemStack(structureSaver))
                    .displayItems((enabledFlags, output) -> {
                        output.accept(new ItemStack(structureSaver));
                    });
        });
    }
}
