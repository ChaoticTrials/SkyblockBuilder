package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class DeleteTagsHandler {

    public static void handle(DeleteTagsHandler.Message msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            ItemStack stack = ItemStructureSaver.removeTags(msg.stack);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        });
        ctx.get().setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<DeleteTagsHandler.Message> {

        @Override
        public Class<DeleteTagsHandler.Message> messageClass() {
            return DeleteTagsHandler.Message.class;
        }

        @Override
        public void encode(DeleteTagsHandler.Message msg, FriendlyByteBuf buffer) {
            buffer.writeItem(msg.stack);
        }

        @Override
        public DeleteTagsHandler.Message decode(FriendlyByteBuf buffer) {
            return new DeleteTagsHandler.Message(buffer.readItem());
        }
    }

    public record Message(ItemStack stack) {
        // empty
    }
}
