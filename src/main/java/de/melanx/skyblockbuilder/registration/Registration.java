package de.melanx.skyblockbuilder.registration;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.world.VoidWorldType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registration {
    private static final DeferredRegister<ForgeWorldType> WORLD_TYPES = DeferredRegister.create(ForgeRegistries.WORLD_TYPES, SkyblockBuilder.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SkyblockBuilder.MODID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, SkyblockBuilder.MODID);

    public static final RegistryObject<ForgeWorldType> SKYBLOCK = WORLD_TYPES.register("custom_skyblock", VoidWorldType::new);

    public static final RegistryObject<Item> STRUCTURE_SAVER = ITEMS.register("structure_saver", ItemStructureSaver::new);
    public static final RegistryObject<ContainerType<ContainerStructureSaver>> CONTAINER_STRUCTURE_SAVER = CONTAINERS.register("structure_saver", ContainerStructureSaver::createContainerType);

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        WORLD_TYPES.register(bus);
        ITEMS.register(bus);
        CONTAINERS.register(bus);
    }
}
