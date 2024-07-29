package de.melanx.skyblockbuilder.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.melanx.skyblockbuilder.ModBlocks;
import de.melanx.skyblockbuilder.ModItems;
import de.melanx.skyblockbuilder.Registration;
import de.melanx.skyblockbuilder.commands.OpenDumpScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterPresetEditorsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "skyblockbuilder", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventListener {

    public ClientEventListener() {
        MinecraftForge.EVENT_BUS.addListener(this::onKeyInput);
        MinecraftForge.EVENT_BUS.addListener(this::registerClientCommands);
    }

    @SubscribeEvent
    public static void onRegisterPresetEditors(RegisterPresetEditorsEvent event) {
        event.register(Registration.skyblockKey, ScreenCustomizeSkyblock::new);
    }

    @SubscribeEvent
    public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(() -> ModItems.structureSaver);
            event.accept(() -> ModBlocks.spawnBlock);
        }
    }

    private void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("skyblock")
                .then(OpenDumpScreen.register())
        );
    }

    private void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.structureSaver)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("Position1") || !tag.contains("Position2")) {
            return;
        }

        Direction direction = switch (event.getKey()) {
            case GLFW.GLFW_KEY_KP_8 -> player.getDirection();
            case GLFW.GLFW_KEY_KP_2 -> player.getDirection().getOpposite();
            case GLFW.GLFW_KEY_KP_4 -> player.getDirection().getCounterClockWise();
            case GLFW.GLFW_KEY_KP_6 -> player.getDirection().getClockWise();
            case GLFW.GLFW_KEY_KP_9 -> Direction.UP;
            case GLFW.GLFW_KEY_KP_3 -> Direction.DOWN;
            default -> null;
        };

        if (direction == null) {
            return;
        }

        BlockPos pos1 = NbtUtils.readBlockPos(tag.getCompound("Position1"));
        BlockPos pos2 = NbtUtils.readBlockPos(tag.getCompound("Position2"));

        tag.put("Position1", NbtUtils.writeBlockPos(pos1.relative(direction)));
        tag.put("Position2", NbtUtils.writeBlockPos(pos2.relative(direction)));

        stack.setTag(tag);
    }
}
