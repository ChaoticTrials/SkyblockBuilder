package de.melanx.skyblockbuilder.client;

import de.melanx.skyblockbuilder.client.screens.StructureSaverScreen;
import de.melanx.skyblockbuilder.util.SkyComponents;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;
import org.moddingx.libx.screen.text.ComponentLayout;
import org.moddingx.libx.screen.text.TextScreen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ClientUtil {

    public static void openPath(Path dir) {
        try {
            Files.createDirectories(dir);
            Util.getPlatform().openUri(dir.toUri());
        } catch (IOException e) {
            //noinspection ConstantConditions
            Minecraft.getInstance().player.displayClientMessage(SkyComponents.SCREEN_OPEN_FOLDER_ERROR.apply(dir.toString()), false);
        }
    }

    public static void openItemScreen(ItemStack stack) {
        Minecraft.getInstance().setScreen(new StructureSaverScreen(stack));
    }

    public static void openErrorScreen(Path configPath, String reason) {
        Minecraft.getInstance().setScreen(new TextScreen(ComponentLayout.simple(
                SkyComponents.SCREEN_ERROR_TITLE,
                List.of(Component.literal("Failed to overwrite config " + FMLPaths.GAMEDIR.get().relativize(configPath)), Component.literal(reason))
        ), 230) {

            @Override
            protected void init() {
                super.init();

                this.addRenderableWidget(Button.builder(CommonComponents.GUI_OK, button -> Minecraft.getInstance().setScreen(null))
                        .pos(this.width / 2 - 115, this.height - 27)
                        .width(110)
                        .build());
                this.addRenderableWidget(Button.builder(Component.literal("Open Config"), button -> {
                            Util.getPlatform().openFile(configPath.toFile());
                        })
                        .pos(this.width / 2 + 5, this.height - 27)
                        .width(110)
                        .build());
            }
        });
    }
}
