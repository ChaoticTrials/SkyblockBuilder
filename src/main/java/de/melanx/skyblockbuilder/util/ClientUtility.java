package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.client.ScreenStructureSaver;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientUtility {

    @OnlyIn(Dist.CLIENT)
    public static void openPath(String path) {
        try {
            Path dir = Paths.get(path);
            Files.createDirectories(dir);
            Util.getPlatform().openUri(dir.toUri());
        } catch (IOException e) {
            //noinspection ConstantConditions
            Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("skyblockbuilder.", path), false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void openItemScreen(ItemStack stack) {
        Minecraft.getInstance().setScreen(new ScreenStructureSaver(stack, new TranslatableComponent("screen.skyblockbuilder.structure_saver")));
    }
}
