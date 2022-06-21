package de.melanx.skyblockbuilder.network;

import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketSerializer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class ProfilesUpdateHandler {

    public static void handle(ProfilesUpdateHandler.Message msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Set<GameProfile> profiles = new HashSet<>();
            for (Tag tag : msg.profiles.getList("Profiles", Tag.TAG_COMPOUND)) {
                CompoundTag nbt = (CompoundTag) tag;

                UUID id = nbt.getUUID("Id");
                String name = nbt.getString("Name");

                profiles.add(new GameProfile(id, name));
            }

            GameProfileCache.addProfiles(profiles);
        });
        ctx.get().setPacketHandled(true);
    }

    public static class ProfilesUpdateSerializer implements PacketSerializer<ProfilesUpdateHandler.Message> {

        @Override
        public Class<ProfilesUpdateHandler.Message> messageClass() {
            return ProfilesUpdateHandler.Message.class;
        }

        @Override
        public void encode(ProfilesUpdateHandler.Message msg, FriendlyByteBuf buffer) {
            buffer.writeNbt(msg.profiles);
        }

        @Override
        public ProfilesUpdateHandler.Message decode(FriendlyByteBuf buffer) {
            return new ProfilesUpdateHandler.Message(buffer.readNbt());
        }
    }

    public record Message(CompoundTag profiles) {
        // empty
    }
}
