package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.data.SkyMeta;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import io.github.noeppi_noeppi.libx.network.PacketSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
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
            SkyMeta meta = null;
            for (Tag inbt : tag.getList("MetaInformation", Tag.TAG_COMPOUND)) {
                CompoundTag mtag = (CompoundTag) inbt;

                UUID player = mtag.getUUID("Player");
                if (msg.player.equals(player)) {
                    meta = SkyMeta.get(msg.data, mtag.getCompound("Meta"));
                    break;
                }
            }
            tag.remove("MetaInformation");
            if (meta != null) {
                ListTag metaInfo = new ListTag();
                CompoundTag playerMeta = new CompoundTag();
                playerMeta.putUUID("Player", msg.player);
                playerMeta.put("Meta", meta.save());
                metaInfo.add(playerMeta);
                tag.put("MetaInformation", metaInfo);
            }
            buffer.writeNbt(tag);
            buffer.writeUUID(msg.player);
        }

        @Override
        public SkyblockDataUpdateHandler.Message decode(FriendlyByteBuf buffer) {
            SkyblockSavedData data = new SkyblockSavedData();
            data.load(Objects.requireNonNull(buffer.readNbt()));
            return new SkyblockDataUpdateHandler.Message(data, buffer.readUUID());
        }
    }

    public record Message(SkyblockSavedData data, UUID player) {
        // empty
    }
}
