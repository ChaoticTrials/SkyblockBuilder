package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.blocks.SpawnBlock;
import de.melanx.skyblockbuilder.world.VoidWorldType;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SkyblockBuilder.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SkyblockBuilder.MODID);
    public static final DeferredRegister<ForgeWorldType> WORLD_TYPES = DeferredRegister.create(ForgeRegistries.WORLD_TYPES, SkyblockBuilder.MODID);
    public static final RegistryObject<Block> SPAWN_BLOCK = BLOCKS.register("spawn", SpawnBlock::new);
    public static final RegistryObject<Item> SPAWN_BLOCK_ITEM = ITEMS.register("spawn", () -> new BlockItem(SPAWN_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<VoidWorldType> CUSTOM_SKYBLOCK = WORLD_TYPES.register("custom_skyblock", VoidWorldType::new);

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        WORLD_TYPES.register(bus);
    }
}
