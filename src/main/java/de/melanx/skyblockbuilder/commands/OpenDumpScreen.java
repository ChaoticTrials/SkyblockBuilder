package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.melanx.skyblockbuilder.client.DumpScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class OpenDumpScreen {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("dump")
                .executes(OpenDumpScreen::openDumpScreen);
    }

    private static int openDumpScreen(CommandContext<CommandSourceStack> context) {
        Minecraft.getInstance().setScreen(new DumpScreen());
        return 1;
    }
}
