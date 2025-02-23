package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class GiveItemHandler extends PacketHandler<GiveItemHandler.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().resource("give_item"));

    protected GiveItemHandler() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            player.addItem(new ItemStack(msg.item));
        }
    }

    public record Message(Item item) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                ((buffer, msg) -> buffer.writeVarInt(Item.getId(msg.item))),
                buffer -> new Message(Item.byId(buffer.readVarInt()))
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return GiveItemHandler.TYPE;
        }
    }
}
