package de.melanx.skyblockbuilder.network;

import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProfilesUpdateHandler extends PacketHandler<ProfilesUpdateHandler.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().resource("profiles_update"));

    protected ProfilesUpdateHandler() {
        super(TYPE, PacketFlow.CLIENTBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        GameProfileCache.addProfiles(msg.profiles);
    }

    public record Message(Set<GameProfile> profiles) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, ProfilesUpdateHandler.Message> CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    int size = msg.profiles.size();
                    buffer.writeVarInt(size);
                    msg.profiles.forEach(profile -> {
                        CompoundTag tag = new CompoundTag();
                        tag.putUUID("Id", profile.getId());
                        tag.putString("Name", profile.getName());
                        buffer.writeNbt(tag);
                    });
                },
                buffer -> {
                    int size = buffer.readVarInt();
                    Set<GameProfile> profiles = new HashSet<>();
                    for (int i = 0; i < size; i++) {
                        CompoundTag tag = buffer.readNbt();
                        if (tag == null) {
                            continue;
                        }

                        UUID id = tag.getUUID("Id");
                        String name = tag.getString("Name");
                        profiles.add(new GameProfile(id, name));
                    }

                    return new ProfilesUpdateHandler.Message(profiles);
                }
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ProfilesUpdateHandler.TYPE;
        }
    }
}
