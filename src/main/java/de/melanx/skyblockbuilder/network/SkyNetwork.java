package de.melanx.skyblockbuilder.network;

import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.util.RandomUtility;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkyNetwork extends NetworkX {

    public SkyNetwork() {
        super(SkyblockBuilder.getInstance());
    }

    @Override
    protected Protocol getProtocol() {
        return Protocol.of("12");
    }

    @Override
    protected void registerPackets() {
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new SaveStructureMessage.Serializer(), () -> SaveStructureMessage.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new DeleteTagsMessage.Serializer(), () -> DeleteTagsMessage.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new CreateSkyblockDump.Serializer(), () -> CreateSkyblockDump.Handler::new);

        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new SkyblockDataUpdateMessage.Serializer(), () -> SkyblockDataUpdateMessage.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new ProfilesUpdateMessage.Serializer(), () -> ProfilesUpdateMessage.Handler::new);
        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new UpdateTemplateNamesMessage.Serializer(), () -> UpdateTemplateNamesMessage.Handler::new);
    }

    public void updateData(Level level, SkyblockSavedData data) {
        if (!level.isClientSide) {
            for (ServerPlayer player : ((ServerLevel) level).getServer().getPlayerList().getPlayers()) {
                this.updateData(player, data);
            }
        }
    }

    public void updateData(Player player, @Nullable SkyblockSavedData data) {
        if (!player.getCommandSenderWorld().isClientSide) {
            this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SkyblockDataUpdateMessage(data != null ? data : SkyblockSavedData.get(player.getCommandSenderWorld()), player.getGameProfile().getId()));
        }
    }

    public void deleteTags(ItemStack stack) {
        this.channel.sendToServer(new DeleteTagsMessage(stack));
    }

    public void createSkyblockDump(boolean includeConfigs, boolean includeTemplates, boolean includeLevelDat, boolean includeLog, boolean includeCrashReport, boolean includeSkyblockBuilderWorldData) {
        this.channel.sendToServer(new CreateSkyblockDump(includeConfigs, includeTemplates, includeLevelDat, includeLog, includeCrashReport, includeSkyblockBuilderWorldData));
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.21")
    public void saveStructure(ItemStack stack, String name, boolean saveToConfig, boolean ignoreAir, boolean asSnbt) {
        this.saveStructure(stack, name, saveToConfig, ignoreAir, asSnbt, false);
    }

    public void saveStructure(ItemStack stack, String name, boolean saveToConfig, boolean ignoreAir, boolean asSnbt, boolean netherValidation) {
        this.channel.sendToServer(new SaveStructureMessage(stack, name, saveToConfig, ignoreAir, asSnbt, netherValidation));
    }

    public void updateProfiles(Player player) {
        if (player.getCommandSenderWorld().isClientSide) {
            return;
        }

        this.sendProfilesInBatches(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), RandomUtility.getGameProfiles((ServerLevel) player.getCommandSenderWorld()));
    }

    public void updateProfiles(Level level) {
        if (level.isClientSide) {
            return;
        }

        Set<GameProfile> gameProfiles = RandomUtility.getGameProfiles((ServerLevel) level);

        this.sendProfilesInBatches(PacketDistributor.ALL.noArg(), gameProfiles);
    }

    public void updateTemplateNames(Player player, List<String> names) {
        if (player.level().isClientSide) {
            return;
        }

        this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new UpdateTemplateNamesMessage(names));
    }

    public void updateTemplateNames(List<String> names) {
        this.channel.send(PacketDistributor.ALL.noArg(), new UpdateTemplateNamesMessage(names));
    }

    private void sendProfilesInBatches(PacketDistributor.PacketTarget target, Set<GameProfile> allGameProfiles) {
        final int batchSize = 1000;
        Set<GameProfile> currentBatch = new HashSet<>(batchSize);

        for (GameProfile profile : allGameProfiles) {
            currentBatch.add(profile);

            if (currentBatch.size() == batchSize) {
                this.sendProfileBatch(target, currentBatch);
                currentBatch.clear();
            }
        }

        // send any remaining profiles in the last batch
        if (!currentBatch.isEmpty()) {
            this.sendProfileBatch(target, currentBatch);
        }
    }

    private void sendProfileBatch(PacketDistributor.PacketTarget target, Set<GameProfile> profiles) {
        this.channel.send(target, new ProfilesUpdateMessage(profiles));
    }
}
