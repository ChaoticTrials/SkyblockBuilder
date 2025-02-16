package de.melanx.skyblockbuilder.registration;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import org.moddingx.libx.annotation.registration.RegisterClass;

import java.util.function.UnaryOperator;

@RegisterClass(registry = "DATA_COMPONENT_TYPE")
public class ModDataComponentTypes {

    public static final DataComponentType<CompoundTag> positions = ModDataComponentTypes.builder(builder -> builder.persistent(CompoundTag.CODEC));
    public static final DataComponentType<CompoundTag> previousPositions = ModDataComponentTypes.builder(builder -> builder.persistent(CompoundTag.CODEC));

    private static <T> DataComponentType<T> builder(UnaryOperator<DataComponentType.Builder<T>> builder) {
        return builder.apply(new DataComponentType.Builder<>()).build();
    }
}
