package de.melanx.skyblockbuilder.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CuriosCompat {

    public static final String MODID = "curios";
    private static final List<Pair<String, ItemStack>> STARTER_ITEMS = new ArrayList<>();

    public static void dropInventory(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
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

    public static void setStartInventory(Player player) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();

                outerLoop:
            for (Pair<String, ItemStack> entry : CuriosCompat.STARTER_ITEMS) {
                ICurioStacksHandler stacksHandler = curios.get(entry.getKey());
                if (stacksHandler == null) {
                    CuriosCompat.error(player);
                    SkyblockBuilder.getLogger().error("Curios slot identifier invalid: {}", entry.getKey());
                    continue;
                }

                IDynamicStackHandler stacks = stacksHandler.getStacks();
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    if (!stacks.getStackInSlot(i).isEmpty()) {
                        continue;
                    }

                    stacks.setStackInSlot(i, entry.getValue());
                    continue outerLoop;
                }

                CuriosCompat.error(player);
                SkyblockBuilder.getLogger().error("No slot available for item '{}' with identifier '{}'", entry.getValue(), entry.getKey());
            }
        });
    }

    public static void loadStarterInventory(JsonArray curiosItems, HolderLookup.Provider lookupProvider) {
        CuriosCompat.STARTER_ITEMS.clear();

        for (JsonElement element : curiosItems) {
            Tag tag = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, element);
            Optional<ItemStack> itemStack = ItemStack.parse(lookupProvider, tag);
            if (itemStack.isEmpty()) {
                throw new IllegalStateException("I have no idea what went wrong lol"); // todo rephrase
            }

            JsonObject item = element.getAsJsonObject();
            ItemStack stack = itemStack.get();

            if (!item.has("Slot")) {
                throw new IllegalStateException("Curios inventory 'Slot' identifier missing for " + stack);
            }

            String identifier = item.get("Slot").getAsString();
            CuriosCompat.STARTER_ITEMS.add(Pair.of(identifier, stack));
        }
    }

    private static void error(Player player) {
        player.sendSystemMessage(Component.literal("Something went wrong, look at the log for more information. " +
                        "If you're not the pack author, report it to them.")
                .withStyle(ChatFormatting.RED));
    }

    public static JsonObject serializeItem(ItemStack stack, String identifier, HolderLookup.Provider lookupProvider) {
        JsonObject json = RandomUtility.serializeItem(stack, lookupProvider);
        json.addProperty("Slot", identifier);

        return json;
    }
}
