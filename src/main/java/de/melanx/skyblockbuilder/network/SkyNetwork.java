package de.melanx.skyblockbuilder.network;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import io.github.noeppi_noeppi.libx.network.NetworkX;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SkyNetwork extends NetworkX {

    public SkyNetwork() {
        super(SkyblockBuilder.getInstance());
    }

    @Override
    protected Protocol getProtocol() {
        return Protocol.of("6");
    }

    @Override
    protected void registerPackets() {
        this.register(new SaveStructureHandler.Serializer(), () -> SaveStructureHandler::handle, NetworkDirection.PLAY_TO_SERVER);
        this.register(new DeleteTagsHandler.Serializer(), () -> DeleteTagsHandler::handle, NetworkDirection.PLAY_TO_SERVER);
        this.register(new RequestDataUpdateHandler.Serializer(), () -> RequestDataUpdateHandler::handle, NetworkDirection.PLAY_TO_SERVER);

        this.register(new SkyblockDataUpdateHandler.Serializer(), () -> SkyblockDataUpdateHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
        this.register(new ProfilesUpdateHandler.ProfilesUpdateSerializer(), () -> ProfilesUpdateHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
        this.register(new UpdateTemplateNamesHandler.Serializer(), () -> UpdateTemplateNamesHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
    }

    public void updateData(Level level) {
        if (!level.isClientSide) {
            this.channel.send(PacketDistributor.ALL.noArg(), new SkyblockDataUpdateHandler.Message(SkyblockSavedData.get(level)));
        }
    }

    public void updateData(Player player) {
        if (!player.getCommandSenderWorld().isClientSide) {
            this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SkyblockDataUpdateHandler.Message(SkyblockSavedData.get(player.getCommandSenderWorld())));
        }
    }

    public void deleteTags(ItemStack stack) {
        this.channel.sendToServer(new DeleteTagsHandler.Message(stack));
    }

    public void saveStructure(ItemStack stack, String name, boolean ignoreAir) {
        this.channel.sendToServer(new SaveStructureHandler.Message(stack, name, ignoreAir));
    }

    public void updateProfiles(Player player) {
        if (player.getCommandSenderWorld().isClientSide) {
            return;
        }

        this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new ProfilesUpdateHandler.Message(this.getProfilesTag((ServerLevel) player.getCommandSenderWorld())));
    }

    public void updateProfiles(Level level) {
        if (level.isClientSide) {
            return;
        }

        this.channel.send(PacketDistributor.ALL.noArg(), new ProfilesUpdateHandler.Message(this.getProfilesTag((ServerLevel) level)));
    }

    public void updateTemplateNames(List<String> names) {
        this.channel.send(PacketDistributor.ALL.noArg(), new UpdateTemplateNamesHandler.Message(names));
    }

    private CompoundTag getProfilesTag(ServerLevel level) {
        MinecraftServer server = level.getServer();

        GameProfileCache profileCache = server.getProfileCache();
        CompoundTag profiles = new CompoundTag();
        ListTag tags = new ListTag();

        Set<UUID> handledIds = Sets.newHashSet();

        // load the cache and look for all profiles
        profileCache.load().forEach(profileInfo -> {
            GameProfile profile = profileInfo.getProfile();

            if (profile.getId() != null && profile.getName() != null) {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("Id", profile.getId());
                tag.putString("Name", profile.getName());
                tags.add(tag);
                handledIds.add(profile.getId());
            }
        });

        // check if all the members were in the cache and add these tags if needed
        for (Team team : SkyblockSavedData.get(level).getTeams()) {
            for (UUID id : team.getPlayers()) {
                if (handledIds.contains(id)) {
                    continue;
                }

                CompoundTag tag = new CompoundTag();
                tag.putUUID("Id", id);

                Optional<GameProfile> gameProfile = profileCache.get(id);
                if (gameProfile.isPresent()) {
                    tag.putString("Name", gameProfile.get().getName());
                } else {
                    GameProfile profile = server.getSessionService().fillProfileProperties(new GameProfile(id, null), true);

                    if (profile.getName() == null) {
                        tag.putString("Name", "Unknown");
                    } else {
                        profileCache.add(profile);
                        tag.putString("Name", profile.getName());
                    }
                }

                tags.add(tag);
            }
        }

        profiles.put("Profiles", tags);
        return profiles;
    }
}
