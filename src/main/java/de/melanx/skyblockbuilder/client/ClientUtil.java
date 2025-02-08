package de.melanx.skyblockbuilder.client;

import de.melanx.skyblockbuilder.client.screens.StructureSaverScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientUtil {

    public static void openPath(Path dir) {
        try {
            Files.createDirectories(dir);
            Util.getPlatform().openUri(dir.toUri());
        } catch (IOException e) {
            //noinspection ConstantConditions
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("skyblockbuilder.screen.open_folder.error", dir.toString()), false);
        }
    }

    public static void openItemScreen(ItemStack stack) {
        Minecraft.getInstance().setScreen(new StructureSaverScreen(stack, Component.translatable("screen.skyblockbuilder.structure_saver")));
    }
}
