package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class RequestDataUpdateHandler {

    public static void handle(RequestDataUpdateHandler.Message msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !Objects.equals(msg.playerId, player.getGameProfile().getId())) {
                return;
            }

            SkyblockBuilder.getNetwork().channel.reply(new SkyblockDataUpdateHandler.Message(SkyblockSavedData.get(player.getCommandSenderWorld())), ctx.get());
        });
        ctx.get().setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<RequestDataUpdateHandler.Message> {

        @Override
        public Class<Message> messageClass() {
            return Message.class;
        }

        @Override
        public void encode(Message msg, FriendlyByteBuf buffer) {
            buffer.writeUUID(msg.playerId);
        }

        @Override
        public Message decode(FriendlyByteBuf buffer) {
            return new Message(buffer.readUUID());
        }
    }

    public record Message(UUID playerId) {
        // empty
    }
}
