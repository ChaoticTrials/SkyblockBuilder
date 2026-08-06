package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SkyblockDataUpdateHandler extends PacketHandler<SkyblockDataUpdateHandler.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().id("skyblock_data_update"));

    protected SkyblockDataUpdateHandler() {
        super(TYPE, PacketFlow.CLIENTBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        SkyblockSavedData.updateClient(msg.data);
    }

    public record Message(SkyblockSavedData data, UUID player) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    // only the receiving player's meta is encoded, everyone else's stays on the server
                    RegistryOps<Tag> ops = buffer.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                    Tag tag = SkyblockSavedData.makeCodec(msg.data.getLevel(), msg.player)
                            .encodeStart(ops, msg.data)
                            .getOrThrow(error -> new IllegalStateException("Failed to encode Skyblock data: " + error));

                    buffer.writeNbt(tag);
                    buffer.writeUUID(msg.player);
                },
                buffer -> {
                    Tag tag = buffer.readNbt(NbtAccounter.unlimitedHeap());
                    if (tag == null) {
                        throw new IllegalStateException("There's something weird happening when updating Skyblock data: no payload");
                    }

                    RegistryOps<Tag> ops = buffer.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                    SkyblockSavedData data = SkyblockSavedData.makeCodec(null)
                            .parse(ops, tag)
                            .getOrThrow(error -> new IllegalStateException("Failed to decode Skyblock data: " + error));

                    return new SkyblockDataUpdateHandler.Message(data, buffer.readUUID());
                }
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
