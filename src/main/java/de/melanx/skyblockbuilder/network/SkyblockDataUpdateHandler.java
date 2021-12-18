package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class SkyblockDataUpdateHandler {

    public static void handle(SkyblockDataUpdateHandler.Message msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> SkyblockSavedData.updateClient(msg.data));
        ctx.get().setPacketHandled(true);
    }

    public static class Serializer implements PacketSerializer<SkyblockDataUpdateHandler.Message> {

        @Override
        public Class<SkyblockDataUpdateHandler.Message> messageClass() {
            return SkyblockDataUpdateHandler.Message.class;
        }

        @Override
        public void encode(SkyblockDataUpdateHandler.Message msg, FriendlyByteBuf buffer) {
            CompoundTag tag = msg.data.save(new CompoundTag());
            if (tag.contains("MetaInformation")) {
                tag.remove("MetaInformation");
            }
            buffer.writeNbt(tag);
        }

        @Override
        public SkyblockDataUpdateHandler.Message decode(FriendlyByteBuf buffer) {
            SkyblockSavedData data = new SkyblockSavedData();
            data.load(Objects.requireNonNull(buffer.readNbt()));
            return new SkyblockDataUpdateHandler.Message(data);
        }
    }

    public record Message(SkyblockSavedData data) {
        // empty
    }
}
