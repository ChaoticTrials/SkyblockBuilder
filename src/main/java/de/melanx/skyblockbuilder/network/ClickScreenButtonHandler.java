package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.registration.ItemStructureSaver;
import de.melanx.skyblockbuilder.registration.ScreenStructureSaver;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClickScreenButtonHandler {

    public static void handle(ClickScreenButtonHandler.Message msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null)
                return;
            ServerWorld world = player.getServerWorld();
            ItemStack stack;
            switch (msg.button) {
                case SAVE:
                    String name = ItemStructureSaver.saveSchematic(world, msg.stack, msg.name);
                    stack = ItemStructureSaver.removeTags(msg.stack);
                    player.setHeldItem(Hand.MAIN_HAND, stack);
                    IFormattableTextComponent component = new TranslationTextComponent("skyblockbuilder.schematic.saved", name);
                    player.sendStatusMessage(component, true);
                    break;
                case DELETE:
                    stack = ItemStructureSaver.removeTags(msg.stack);
                    player.setHeldItem(Hand.MAIN_HAND, stack);
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static class ClickScreenButtonSerializer implements PacketSerializer<Message> {

        @Override
        public Class<Message> messageClass() {
            return Message.class;
        }

        @Override
        public void encode(Message msg, PacketBuffer buffer) {
            buffer.writeItemStack(msg.stack);
            buffer.writeEnumValue(msg.button);
            buffer.writeString(msg.name);
        }

        @Override
        public Message decode(PacketBuffer buffer) {
            return new Message(buffer.readItemStack(), buffer.readEnumValue(ScreenStructureSaver.Button.class), buffer.readString());
        }
    }

    public static class Message {

        public final ItemStack stack;
        public final ScreenStructureSaver.Button button;
        public final String name;

        public Message(ItemStack stack, ScreenStructureSaver.Button button, String name) {
            this.stack = stack;
            this.button = button;
            this.name = name;
        }
    }
}
