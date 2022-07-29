package de.melanx.skyblockbuilder.network;

import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public record ProfilesUpdateMessage(CompoundTag profiles) {

    public static class Handler implements PacketHandler<ProfilesUpdateMessage> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(ProfilesUpdateMessage msg, Supplier<NetworkEvent.Context> ctx) {
            Set<GameProfile> profiles = new HashSet<>();
            for (Tag tag : msg.profiles.getList("Profiles", Tag.TAG_COMPOUND)) {
                CompoundTag nbt = (CompoundTag) tag;

                UUID id = nbt.getUUID("Id");
                String name = nbt.getString("Name");

                profiles.add(new GameProfile(id, name));
            }

            GameProfileCache.addProfiles(profiles);
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
            buffer.writeNbt(msg.profiles);
        }

        @Override
        public ProfilesUpdateMessage decode(FriendlyByteBuf buffer) {
            return new ProfilesUpdateMessage(buffer.readNbt());
        }
    }
}
