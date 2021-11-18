package de.melanx.skyblockbuilder.commands.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.util.RandomUtility;
import io.github.noeppi_noeppi.libx.util.NbtToJson;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class InventoryCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("inventory")
                .then(Commands.literal("export")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> exportInventory(context.getSource())));
    }

    private static int exportInventory(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String folderName = "skyblock_exports";
        try {
            Files.createDirectories(Paths.get(folderName));
        } catch (IOException e) {
            throw new SimpleCommandExceptionType(new TranslatableComponent("skyblockbuilder.command.error.creating_path", folderName)).create();
        }
        String filePath = RandomUtility.getFilePath(folderName, "starter_item", "json");

        JsonObject json = new JsonObject();
        JsonArray items = new JsonArray();

        Inventory inventory = player.getInventory();

        for (ItemStack stack : inventory.items) {
            if (!stack.isEmpty()) {
                CompoundTag tag = stack.serializeNBT();
                tag.putInt("Count", stack.getCount());
                items.add(NbtToJson.getJson(tag, true));
            }
        }

        for (ItemStack stack : inventory.offhand) {
            if (!stack.isEmpty()) {
                CompoundTag tag = stack.serializeNBT();
                tag.putInt("Count", stack.getCount());
                tag.putString("Slot", "offhand");
                items.add(NbtToJson.getJson(tag, true));
            }
        }

        for (int slot : Inventory.ALL_ARMOR_SLOTS) {
            ItemStack stack = inventory.armor.get(slot);
            if (!stack.isEmpty()) {
                CompoundTag tag = stack.serializeNBT();
                tag.putInt("Count", stack.getCount());
                String slotName = switch (slot) {
                    case 0 -> "feet";
                    case 1 -> "legs";
                    case 2 -> "chest";
                    case 3 -> "head";
                    default -> "mainhand";
                };
                tag.putString("Slot", slotName);
                items.add(NbtToJson.getJson(tag, true));
//                JsonObject item = new JsonObject();
//                //noinspection ConstantConditions
//                item.addProperty("item", stack.getItem().getRegistryName().toString());
//                CompoundTag tag = stack.getTag();
//                if (tag != null) {
//                    item.add("nbt", NbtToJson.getJson(tag, true));
//                }
            }
        }

        json.add("items", items);
        Path file = Paths.get(folderName).resolve(filePath.split("/")[1]);
        try {
            BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            w.write(SkyblockBuilder.PRETTY_GSON.toJson(json));
            w.close();
        } catch (IOException e) {
            throw new SimpleCommandExceptionType(new TranslatableComponent("skyblockbuilder.command.error.creating_file", file)).create();
        }

        source.sendSuccess(new TranslatableComponent("skyblockbuilder.command.success.export_inventory", filePath).withStyle(ChatFormatting.GOLD), true);
        return 1;
    }
}
