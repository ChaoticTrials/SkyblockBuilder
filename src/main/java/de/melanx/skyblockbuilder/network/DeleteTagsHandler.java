package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.item.ItemStructureSaver;
import de.melanx.skyblockbuilder.registration.ModItems;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class DeleteTagsHandler extends PacketHandler<DeleteTagsHandler.Message> {

    public static final CustomPacketPayload.Type<DeleteTagsHandler.Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().id("delete_tags"));

    protected DeleteTagsHandler() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.structureSaver)) {
            return;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStructureSaver.removeComponents(stack));
    }

    public record Message() implements CustomPacketPayload {

        public static final StreamCodec<Object, DeleteTagsHandler.Message> CODEC = StreamCodec.unit(new Message());

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return DeleteTagsHandler.TYPE;
        }
    }
}
