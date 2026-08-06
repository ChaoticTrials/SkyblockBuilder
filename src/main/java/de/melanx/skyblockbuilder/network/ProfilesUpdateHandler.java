package de.melanx.skyblockbuilder.network;

import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ProfilesUpdateHandler extends PacketHandler<ProfilesUpdateHandler.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(SkyblockBuilder.getInstance().id("profiles_update"));

    protected ProfilesUpdateHandler() {
        super(TYPE, PacketFlow.CLIENTBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        GameProfileCache.addProfiles(msg.profiles);
    }

    public record Message(Set<GameProfile> profiles) implements CustomPacketPayload {

        public Message(Set<GameProfile> profiles) {
            this.profiles = Set.copyOf(profiles);
        }

        private static final StreamCodec<RegistryFriendlyByteBuf, GameProfile> PROFILE = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, GameProfile::id,
                ByteBufCodecs.STRING_UTF8, GameProfile::name,
                GameProfile::new
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, Set<GameProfile>> PROFILES = ByteBufCodecs.collection(HashSet::new, PROFILE);

        public static final StreamCodec<RegistryFriendlyByteBuf, ProfilesUpdateHandler.Message> CODEC = PROFILES.map(Message::new, Message::profiles);

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ProfilesUpdateHandler.TYPE;
        }
    }
}
