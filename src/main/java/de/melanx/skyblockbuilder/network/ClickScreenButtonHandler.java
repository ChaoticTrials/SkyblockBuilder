package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.client.ScreenStructureSaver;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class ClickScreenButtonHandler {

    public static void handle(ClickScreenButtonHandler.Message msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null)
                return;
            ServerLevel level = player.getLevel();
            ItemStack stack;
            switch (msg.button) {
                case SAVE:
                    String name = ItemStructureSaver.saveSchematic(level, msg.stack, msg.name);
                    stack = ItemStructureSaver.removeTags(msg.stack);
                    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                    MutableComponent component = new TranslatableComponent("skyblockbuilder.schematic.saved", name);
                    player.displayClientMessage(component, true);
                    break;
                case DELETE:
                    stack = ItemStructureSaver.removeTags(msg.stack);
                    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
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
        public void encode(Message msg, FriendlyByteBuf buffer) {
            buffer.writeItem(msg.stack);
            buffer.writeEnum(msg.button);
            buffer.writeUtf(msg.name);
        }

        @Override
        public Message decode(FriendlyByteBuf buffer) {
            return new Message(buffer.readItem(), buffer.readEnum(ScreenStructureSaver.Button.class), buffer.readUtf(32767));
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
