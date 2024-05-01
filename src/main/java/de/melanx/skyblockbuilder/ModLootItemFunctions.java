package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.template.SpreadMapFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "LOOT_FUNCTION_TYPE")
public class ModLootItemFunctions {

    public static final LootItemFunctionType spreadMap = new LootItemFunctionType(new SpreadMapFunction.Serializer());
}
