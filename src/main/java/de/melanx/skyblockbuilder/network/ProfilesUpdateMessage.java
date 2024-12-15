package de.melanx.skyblockbuilder.network;

import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public record ProfilesUpdateMessage(Set<GameProfile> profiles) {

    public static class Handler implements PacketHandler<ProfilesUpdateMessage> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(ProfilesUpdateMessage msg, Supplier<NetworkEvent.Context> ctx) {
            GameProfileCache.addProfiles(msg.profiles);
            return true;
        }
    }

    public static class Serializer implements PacketSerializer<ProfilesUpdateMessage> {

        @Override
        public Class<ProfilesUpdateMessage> messageClass() {
            return ProfilesUpdateMessage.class;
        }

        @Override
        public void encode(ProfilesUpdateMessage msg, FriendlyByteBuf buffer) {
            int size = msg.profiles.size();
            buffer.writeVarInt(size);
            msg.profiles.forEach(profile -> {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("Id", profile.getId());
                tag.putString("Name", profile.getName());
                buffer.writeNbt(tag);
            });
        }

        @Override
        public ProfilesUpdateMessage decode(FriendlyByteBuf buffer) {
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

            return new ProfilesUpdateMessage(profiles);
        }
    }
}
