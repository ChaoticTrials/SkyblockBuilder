package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import net.minecraft.core.component.DataComponentType;
import org.moddingx.libx.annotation.registration.RegisterClass;

import java.util.function.UnaryOperator;

@RegisterClass(registry = "DATA_COMPONENT_TYPE")
public class ModDataComponentTypes {

    public static final DataComponentType<ItemStructureSaver.Positions> positions = ModDataComponentTypes.builder(builder -> builder.persistent(ItemStructureSaver.Positions.CODEC).networkSynchronized(ItemStructureSaver.Positions.STREAM_CODEC));
    public static final DataComponentType<ItemStructureSaver.Positions> previousPositions = ModDataComponentTypes.builder(builder -> builder.persistent(ItemStructureSaver.Positions.CODEC).networkSynchronized(ItemStructureSaver.Positions.STREAM_CODEC));

    private static <T> DataComponentType<T> builder(UnaryOperator<DataComponentType.Builder<T>> builder) {
        return builder.apply(new DataComponentType.Builder<>()).build();
    }
}
