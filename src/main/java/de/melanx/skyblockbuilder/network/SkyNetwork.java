package de.melanx.skyblockbuilder.network;

import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import org.moddingx.libx.annotation.meta.RemoveIn;
import org.moddingx.libx.network.NetworkX;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class SkyNetwork extends NetworkX {

    public SkyNetwork() {
        super(SkyblockBuilder.getInstance());
    }

    @Override
    protected Protocol getProtocol() {
        return Protocol.of("9");
    }

    @Override
    protected void registerPackets() {
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new SaveStructureMessage.Serializer(), () -> SaveStructureMessage.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new DeleteTagsMessage.Serializer(), () -> DeleteTagsMessage.Handler::new);

        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new SkyblockDataUpdateMessage.Serializer(), () -> SkyblockDataUpdateMessage.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new ProfilesUpdateMessage.Serializer(), () -> ProfilesUpdateMessage.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new UpdateTemplateNamesMessage.Serializer(), () -> UpdateTemplateNamesMessage.Handler::new);
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public void updateData(Level level) {
        if (!level.isClientSide) {
            this.updateData(level, SkyblockSavedData.get(level));
        }
    }

    public void updateData(Level level, SkyblockSavedData data) {
        if (!level.isClientSide) {
            for (ServerPlayer player : ((ServerLevel) level).getServer().getPlayerList().getPlayers()) {
                this.updateData(player, data);
            }
        }
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public void updateData(Player player) {
        this.updateData(player, null);
    }

    public void updateData(Player player, @Nullable SkyblockSavedData data) {
        if (!player.getCommandSenderWorld().isClientSide) {
            this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SkyblockDataUpdateMessage(data != null ? data : SkyblockSavedData.get(player.getCommandSenderWorld()), player.getGameProfile().getId()));
        }
    }

    public void deleteTags(ItemStack stack) {
        this.channel.sendToServer(new DeleteTagsMessage(stack));
    }

    public void saveStructure(ItemStack stack, String name, boolean ignoreAir) {
        this.channel.sendToServer(new SaveStructureMessage(stack, name, ignoreAir));
    }

    public void updateProfiles(Player player) {
        if (player.getCommandSenderWorld().isClientSide) {
            return;
        }

        this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new ProfilesUpdateMessage(this.getProfilesTag((ServerLevel) player.getCommandSenderWorld())));
    }

    public void updateProfiles(Level level) {
        if (level.isClientSide) {
            return;
        }

        this.channel.send(PacketDistributor.ALL.noArg(), new ProfilesUpdateMessage(this.getProfilesTag((ServerLevel) level)));
    }

    public void updateTemplateNames(Player player, List<String> names) {
        if (player.level.isClientSide) {
            return;
        }

        this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new UpdateTemplateNamesMessage(names));
    }

    public void updateTemplateNames(List<String> names) {
        this.channel.send(PacketDistributor.ALL.noArg(), new UpdateTemplateNamesMessage(names));
    }

    private CompoundTag getProfilesTag(ServerLevel level) {
        Set<GameProfile> profileCache = RandomUtility.getGameProfiles(level);
        CompoundTag profiles = new CompoundTag();
        ListTag tags = new ListTag();

        // load the cache and look for all profiles
        profileCache.forEach(profile -> {
            if (profile.getId() != null && profile.getName() != null) {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("Id", profile.getId());
                tag.putString("Name", profile.getName());
                tags.add(tag);
            }
        });

        profiles.put("Profiles", tags);
        return profiles;
    }
}
