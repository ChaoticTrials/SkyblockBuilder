package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record DeleteTagsMessage(ItemStack stack) {

    public static class Handler implements PacketHandler<DeleteTagsMessage> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(DeleteTagsMessage msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return true;
            }

            ItemStack stack = ItemStructureSaver.removeTags(msg.stack);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<DeleteTagsMessage> {

        @Override
        public Class<DeleteTagsMessage> messageClass() {
            return DeleteTagsMessage.class;
        }

        @Override
        public void encode(DeleteTagsMessage msg, FriendlyByteBuf buffer) {
            buffer.writeItem(msg.stack);
        }

        @Override
        public DeleteTagsMessage decode(FriendlyByteBuf buffer) {
            return new DeleteTagsMessage(buffer.readItem());
        }
    }
}
