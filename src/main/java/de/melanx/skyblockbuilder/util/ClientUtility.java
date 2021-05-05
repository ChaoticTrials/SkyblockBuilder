package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.client.ScreenStructureSaver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientUtility {

    @OnlyIn(Dist.CLIENT)
    public static void playSound(SoundEvent sound) {
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(sound, 1));
    }

    @OnlyIn(Dist.CLIENT)
    public static void openPath(String path) {
        try {
            Path dir = Paths.get(path);
            Files.createDirectories(dir);
            Util.getOSType().openURI(dir.toUri());
        } catch (IOException e) {
            //noinspection ConstantConditions
            Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("skyblockbuilder.", path), false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void openItemScreen(ItemStack stack) {
        Minecraft.getInstance().displayGuiScreen(new ScreenStructureSaver(stack, new TranslationTextComponent("screen.skyblockbuilder.structure_saver")));
    }
}
