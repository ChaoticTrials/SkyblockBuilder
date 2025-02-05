package de.melanx.skyblockbuilder.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.melanx.skyblockbuilder.compat.CuriosCompat;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;

public class StartingInventory {

    public static final StartingInventory INSTANCE = new StartingInventory();

    private static final List<Pair<EquipmentSlot, ItemStack>> STARTER_ITEMS = new ArrayList<>();
    private Consumer<Player> starterInv = player -> {
    };

    private StartingInventory() {}

    public void loadStarterItems() {
        StartingInventory.STARTER_ITEMS.clear();

        File startInventoryConfig = new File(SkyPaths.ITEMS_FILE.toUri());

        this.starterInv = player -> {
            try {
                String s = IOUtils.toString(new InputStreamReader(new FileInputStream(startInventoryConfig)));
                JsonObject json = GsonHelper.parse(s, true);

                if (json.has("items")) {
                    JsonArray items = json.getAsJsonArray("items");
                    Set<EquipmentSlot> usedTypes = new HashSet<>();
                    int slotsUsedInMainInventory = 0;
                    for (JsonElement item : items) {
                        Tag tag = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, item);
                        Optional<ItemStack> itemStack = ItemStack.parse(player.registryAccess(), tag);
                        if (itemStack.isEmpty()) {
                            throw new IllegalStateException("I have no idea what's wrong lol"); // todo rephrase
                        }

                        JsonObject itemObj = (JsonObject) item;
                        ItemStack stack = itemStack.get();
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

                if (ModList.get().isLoaded(CuriosCompat.MODID) && json.has("curios_items")) {
                    CuriosCompat.loadStarterInventory(json.getAsJsonArray("curios_items"), player.registryAccess());
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read starting inventory", e);
            }
        };
    }

    public static List<Pair<EquipmentSlot, ItemStack>> getStarterItems() {
        return ImmutableList.copyOf(StartingInventory.STARTER_ITEMS);
    }

    public static JsonObject serializeItem(ItemStack stack, HolderLookup.Provider lookupProvider) {
        return StartingInventory.serializeItem(stack, EquipmentSlot.MAINHAND, lookupProvider);
    }

    public static JsonObject serializeItem(ItemStack stack, EquipmentSlot slot, HolderLookup.Provider lookupProvider) {
        JsonObject json = RandomUtility.serializeItem(stack, lookupProvider);
        if (slot != EquipmentSlot.MAINHAND) {
            json.addProperty("Slot", slot.toString().toLowerCase(Locale.ROOT));
        }

        return json;
    }

    public Consumer<Player> getStarterInv() {
        return this.starterInv;
    }
}
