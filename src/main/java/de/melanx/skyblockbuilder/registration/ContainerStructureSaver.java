package de.melanx.skyblockbuilder.registration;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;

import javax.annotation.Nonnull;

public class ContainerStructureSaver extends Container {
    protected ContainerStructureSaver(int id) {
        super(Registration.CONTAINER_STRUCTURE_SAVER.get(), id);
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity playerIn) {
        return true;
    }

    public static <X extends Container> ContainerType<X> createContainerType() {
        //noinspection unchecked
        return (ContainerType<X>) IForgeContainerType.create((windowId, inv, data) -> new ContainerStructureSaver(windowId));
    }
}
