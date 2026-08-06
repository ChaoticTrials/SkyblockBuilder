package de.melanx.skyblockbuilder.registration;

import com.mojang.serialization.MapCodec;
import de.melanx.skyblockbuilder.template.SpreadMapFunction;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "LOOT_FUNCTION_TYPE")
public class ModLootItemFunctions {

    public static final MapCodec<SpreadMapFunction> spreadMap = SpreadMapFunction.CODEC;
}
