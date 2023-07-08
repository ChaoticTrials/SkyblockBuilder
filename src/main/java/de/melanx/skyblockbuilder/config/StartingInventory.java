package de.melanx.skyblockbuilder.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class StartingInventory {

    private static final List<Pair<EquipmentSlot, ItemStack>> STARTER_ITEMS = new ArrayList<>();

    public static void loadStarterItems() {
        StartingInventory.STARTER_ITEMS.clear();

        File spawns = new File(SkyPaths.ITEMS_FILE.toUri());

        try {
            String s = IOUtils.toString(new InputStreamReader(new FileInputStream(spawns)));
            JsonObject json = GsonHelper.parse(s, true);

            if (json.has("items")) {
                JsonArray items = json.getAsJsonArray("items");
                Set<EquipmentSlot> usedTypes = new HashSet<>();
                int slotsUsedInMainInventory = 0;
                for (JsonElement item : items) {
                    JsonObject itemObj = (JsonObject) item;
                    ItemStack stack = CraftingHelper.getItemStack(itemObj, true);
                    EquipmentSlot slot = (itemObj).has("Slot") ? EquipmentSlot.byName(GsonHelper.getAsString(itemObj, "Slot")) : EquipmentSlot.MAINHAND;
                    if (slot == EquipmentSlot.MAINHAND) {
                        if (slotsUsedInMainInventory >= 36) {
                            throw new IllegalStateException("Too many starting items in main inventory. Not more than 36 are allowed.");
                        } else {
                            slotsUsedInMainInventory += 1;
                        }
                    } else {
                        if (usedTypes.contains(slot)) {
                            throw new IllegalStateException("Slot type that is not 'mainhand' was used multiple times for starting inventory.");
                        } else {
                            usedTypes.add(slot);
                        }
                    }
                    StartingInventory.STARTER_ITEMS.add(Pair.of(slot, stack));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read starting inventory", e);
        }
    }

    public static List<Pair<EquipmentSlot, ItemStack>> getStarterItems() {
        return ImmutableList.copyOf(StartingInventory.STARTER_ITEMS);
    }

    public static JsonObject serializeItem(ItemStack stack) {
        return StartingInventory.serializeItem(stack, EquipmentSlot.MAINHAND);
    }

    public static JsonObject serializeItem(ItemStack stack, EquipmentSlot slot) {
        JsonObject json = new JsonObject();
        CompoundTag tag = stack.serializeNBT();
        json.addProperty("item", tag.getString("id"));

        int count = tag.getInt("Count");
        if (count > 1) {
            json.addProperty("count", count);
        }

        if (tag.contains("tag")) {
            //noinspection ConstantConditions
            json.addProperty("nbt", tag.get("tag").toString());
        }

        if (tag.contains("ForgeCaps")) {
            //noinspection ConstantConditions
            json.addProperty("ForgeCaps", tag.get("ForgeCaps").toString());
        }

        if (slot != EquipmentSlot.MAINHAND) {
            json.addProperty("Slot", slot.toString().toLowerCase(Locale.ROOT));
        }

        return json;
    }
}
