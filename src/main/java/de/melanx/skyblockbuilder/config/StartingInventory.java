package de.melanx.skyblockbuilder.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.compat.CuriosCompat;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class StartingInventory {

    private static final List<Pair<EquipmentSlot, ItemStack>> STARTER_ITEMS = new ArrayList<>();

    private StartingInventory() {}

    public static void loadStarterItems(RegistryAccess registryAccess) {
        StartingInventory.STARTER_ITEMS.clear();

        File startInventoryConfig = new File(SkyPaths.ITEMS_FILE.toUri());
        RegistryOps<JsonElement> registryOps = registryAccess.createSerializationContext(JsonOps.INSTANCE);

        try {
            String s = IOUtils.toString(new InputStreamReader(new FileInputStream(startInventoryConfig)));
            JsonObject json = GsonHelper.parse(s, true);

            if (json.has("items")) {
                JsonArray items = json.getAsJsonArray("items");
                Set<EquipmentSlot> usedTypes = new HashSet<>();
                int slotsUsedInMainInventory = 0;
                for (JsonElement element : items) {
                    JsonObject mainObject = element.getAsJsonObject();
                    Optional<com.mojang.datafixers.util.Pair<ItemStack, JsonElement>> item = ItemStack.CODEC.decode(registryOps, mainObject.get("Item"))
                            .resultOrPartial(SkyblockBuilder.getLogger()::error);

                    if (item.isEmpty()) {
                        throw new IllegalStateException("Unable to read starting item: " + mainObject.get("Item"));
                    }

                    EquipmentSlot slot = mainObject.has("Slot") ? EquipmentSlot.byName(GsonHelper.getAsString(mainObject, "Slot")) : EquipmentSlot.MAINHAND;
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
                    StartingInventory.STARTER_ITEMS.add(Pair.of(slot, item.get().getFirst()));
                }
            }

            if (ModList.get().isLoaded(CuriosCompat.MODID) && json.has("curios_items")) {
                CuriosCompat.loadStarterInventory(json.getAsJsonArray("curios_items"), registryAccess);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read starting inventory", e);
        }
    }

    public static List<Pair<EquipmentSlot, ItemStack>> getStarterItems() {
        return ImmutableList.copyOf(StartingInventory.STARTER_ITEMS);
    }
}
