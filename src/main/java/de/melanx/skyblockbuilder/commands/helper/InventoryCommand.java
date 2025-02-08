package de.melanx.skyblockbuilder.commands.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.JsonOps;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.compat.CuriosCompat;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.SkyPaths;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class InventoryCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("inventory")
                .then(Commands.literal("export")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> exportInventory(context.getSource())));
    }

    private static int exportInventory(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        try {
            Files.createDirectories(SkyPaths.MOD_EXPORTS);
        } catch (IOException e) {
            throw new SimpleCommandExceptionType(Component.translatable("skyblockbuilder.command.error.creating_path", SkyPaths.MOD_EXPORTS.toString())).create();
        }
        Path filePath = RandomUtility.getFilePath(SkyPaths.MOD_EXPORTS, "starter_inventory", "json5");

        JsonObject json = new JsonObject();

        json.add("items", InventoryCommand.vanillaInventory(player));
        if (ModList.get().isLoaded(CuriosCompat.MODID)) {
            json.add("curios_items", InventoryCommand.curiosInventory(player));
        }
        Path file = SkyPaths.MOD_EXPORTS.resolve(filePath.getFileName());
        try {
            BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            w.write(SkyblockBuilder.PRETTY_GSON.toJson(json));
            w.close();
        } catch (IOException e) {
            throw new SimpleCommandExceptionType(Component.translatable("skyblockbuilder.command.error.creating_file", file.toString())).create();
        }

        source.sendSuccess(() -> Component.translatable("skyblockbuilder.command.success.export_inventory", filePath.toString()).withStyle(ChatFormatting.GOLD), true);
        return 1;
    }

    private static JsonArray vanillaInventory(ServerPlayer player) {
        RegistryOps<JsonElement> registryOps = player.registryAccess().createSerializationContext(JsonOps.INSTANCE);

        JsonArray items = new JsonArray();
        Inventory inventory = player.getInventory();

        for (ItemStack item : player.getInventory().items) {
            InventoryCommand.addItemWithSlot(items, item, registryOps);
        }

        for (ItemStack item : player.getInventory().offhand) {
            InventoryCommand.addItemWithSlot(items, item, EquipmentSlot.OFFHAND, registryOps);
        }

        for (int slot : Inventory.ALL_ARMOR_SLOTS) {
            ItemStack item = inventory.armor.get(slot);
            if (!item.isEmpty()) {
                EquipmentSlot equipmentSlot = switch (slot) {
                    case 0 -> EquipmentSlot.FEET;
                    case 1 -> EquipmentSlot.LEGS;
                    case 2 -> EquipmentSlot.CHEST;
                    case 3 -> EquipmentSlot.HEAD;
                    default -> EquipmentSlot.MAINHAND;
                };

                InventoryCommand.addItemWithSlot(items, item, equipmentSlot, registryOps);
            }
        }

        return items;
    }

    private static JsonArray curiosInventory(ServerPlayer player) {
        RegistryOps<JsonElement> registryOps = player.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        JsonArray items = new JsonArray();

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                String identifier = entry.getKey();
                IDynamicStackHandler stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);

                    InventoryCommand.addItemWithSlot(items, stack, identifier, registryOps);
                }
            }
        });

        return items;
    }

    private static void addItemWithSlot(JsonArray items, ItemStack item, RegistryOps<JsonElement> registryOps) {
        InventoryCommand.addItemWithSlot(items, item, EquipmentSlot.MAINHAND, registryOps);
    }

    private static void addItemWithSlot(JsonArray items, ItemStack item, EquipmentSlot slot, RegistryOps<JsonElement> registryOps) {
        InventoryCommand.addItemWithSlot(items, item, slot.toString().toLowerCase(Locale.ROOT), registryOps);
    }

    private static void addItemWithSlot(JsonArray items, ItemStack item, String slot, RegistryOps<JsonElement> registryOps) {
        if (item.isEmpty()) {
            return;
        }

        Optional<JsonElement> optional = ItemStack.OPTIONAL_CODEC
                .encodeStart(registryOps, item)
                .resultOrPartial(SkyblockBuilder.getLogger()::error);
        if (optional.isEmpty()) {
            return;
        }

        JsonObject mainObject = new JsonObject();
        mainObject.addProperty("Slot", slot);
        mainObject.add("Item", optional.get());

        items.add(mainObject);
    }
}
