package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveStructureHandler {

    public static void handle(SaveStructureHandler.Message msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            ServerLevel level = player.getLevel();
            String name = ItemStructureSaver.saveSchematic(level, msg.stack, msg.ignoreAir, msg.name);
            ItemStack stack = ItemStructureSaver.removeTags(msg.stack);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            MutableComponent component = new TranslatableComponent("skyblockbuilder.schematic.saved", name);
            player.displayClientMessage(component, true);
        });
        ctx.get().setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<Message> {

        @Override
        public Class<Message> messageClass() {
            return Message.class;
        }

        @Override
        public void encode(Message msg, FriendlyByteBuf buffer) {
            buffer.writeItem(msg.stack);
            buffer.writeUtf(msg.name);
            buffer.writeBoolean(msg.ignoreAir);
        }

        @Override
        public Message decode(FriendlyByteBuf buffer) {
            return new Message(buffer.readItem(), buffer.readUtf(Short.MAX_VALUE), buffer.readBoolean());
        }
    }

    public record Message(ItemStack stack, String name, boolean ignoreAir) {
        // empty
    }
}
