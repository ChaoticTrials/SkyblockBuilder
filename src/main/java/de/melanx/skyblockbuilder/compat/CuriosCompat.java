package de.melanx.skyblockbuilder.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosCompat {

    public static void dropInventory(Player player) {
        if (player.level.isClientSide) {
            return;
        }

        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            handler.getCurios().forEach((id, type) -> {
                IDynamicStackHandler stacks = type.getStacks();
                IDynamicStackHandler cosmeticStacks = type.getCosmeticStacks();
                for (int i = 0; i < type.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    ItemStack stack1 = cosmeticStacks.getStackInSlot(i);
                    stacks.setStackInSlot(i, ItemStack.EMPTY);
                    cosmeticStacks.setStackInSlot(i, ItemStack.EMPTY);
                    player.drop(stack, true, false);
                    player.drop(stack1, true, false);
                }
            });
        });
    }
}
