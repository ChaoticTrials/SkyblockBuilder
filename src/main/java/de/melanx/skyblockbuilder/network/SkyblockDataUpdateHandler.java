package de.melanx.skyblockbuilder.network;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyMeta;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SkyblockDataUpdateHandler extends PacketHandler<SkyblockDataUpdateHandler.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().resource("skyblock_data_update"));

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
                    //noinspection DataFlowIssue
                    CompoundTag tag = msg.data.save(new CompoundTag(), msg.data.getLevel().registryAccess());
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
                },
                buffer -> {
                    SkyblockSavedData data = new SkyblockSavedData();
                    Tag tag = buffer.readNbt(NbtAccounter.unlimitedHeap());
                    SkyblockSavedData.load((CompoundTag) tag);
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
