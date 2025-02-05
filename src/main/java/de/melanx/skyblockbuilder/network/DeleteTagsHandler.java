package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

public class DeleteTagsHandler extends PacketHandler<DeleteTagsHandler.Message> {

    public static final CustomPacketPayload.Type<DeleteTagsHandler.Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().resource("delete_tags"));

    protected DeleteTagsHandler() {
        super(TYPE, PacketFlow.SERVERBOUND, ItemStack.STREAM_CODEC.map(Message::new, Message::stack), HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack stack = ItemStructureSaver.removeTags(msg.stack);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    }

    public record Message(ItemStack stack) implements CustomPacketPayload {

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return DeleteTagsHandler.TYPE;
        }
    }
}
