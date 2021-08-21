package de.melanx.skyblockbuilder.compat;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosCompat {

    public static void dropInventory(PlayerEntity player) {
        if (player.world.isRemote) {
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
                    ItemEntity entity = new ItemEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ(), stack);
                    ItemEntity entity1 = new ItemEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ(), stack1);
                    player.world.addEntity(entity);
                    player.world.addEntity(entity1);
                }
            });
        });
    }
}
