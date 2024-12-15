package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.data.SkyMeta;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public record SkyblockDataUpdateMessage(SkyblockSavedData data, UUID player) {

    public static class Handler implements PacketHandler<SkyblockDataUpdateMessage> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(SkyblockDataUpdateMessage msg, Supplier<NetworkEvent.Context> ctx) {
            SkyblockSavedData.updateClient(msg.data);
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<SkyblockDataUpdateMessage> {

        @Override
        public Class<SkyblockDataUpdateMessage> messageClass() {
            return SkyblockDataUpdateMessage.class;
        }

        @Override
        public void encode(SkyblockDataUpdateMessage msg, FriendlyByteBuf buffer) {
            CompoundTag tag = msg.data.save(new CompoundTag());
            if (tag.contains("MetaInformation")) {
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
            }
            buffer.writeNbt(tag);
            buffer.writeUUID(msg.player);
        }

        @Override
        public SkyblockDataUpdateMessage decode(FriendlyByteBuf buffer) {
            SkyblockSavedData data = new SkyblockSavedData();
            data.load(Objects.requireNonNull(buffer.readAnySizeNbt()));
            return new SkyblockDataUpdateMessage(data, buffer.readUUID());
        }
    }
}
