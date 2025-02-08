package de.melanx.skyblockbuilder.registration;

import de.melanx.skyblockbuilder.template.SpreadMapFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "LOOT_FUNCTION_TYPE")
public class ModLootItemFunctions {

    public static final LootItemFunctionType<SpreadMapFunction> spreadMap = new LootItemFunctionType<>(SpreadMapFunction.CODEC);
}
