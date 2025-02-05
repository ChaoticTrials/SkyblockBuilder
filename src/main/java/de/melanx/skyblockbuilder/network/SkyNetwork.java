package de.melanx.skyblockbuilder.network;

import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkyNetwork extends NetworkX {

    private static SkyNetwork instance = null;

    public SkyNetwork(ModX mod) {
        super(mod);

        // send to server
        this.register(new SaveStructureHandler());
        this.register(new DeleteTagsHandler());
        this.register(new CreateSkyblockDumpHandler());

        // send to client
        this.register(new SkyblockDataUpdateHandler());
        this.register(new ProfilesUpdateHandler());
        this.register(new UpdateTemplateNamesHandler());
    }

    @Override
    protected String getVersion() {
        return "13";
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
            SkyblockDataUpdateHandler.Message msg = new SkyblockDataUpdateHandler.Message(data != null ? data : SkyblockSavedData.get(player.getCommandSenderWorld()), player.getGameProfile().getId());
            PacketDistributor.sendToPlayer((ServerPlayer) player, msg);
        }
    }

    public void deleteTags(ItemStack stack) {
        PacketDistributor.sendToServer(new DeleteTagsHandler.Message(stack));
    }

    public void createSkyblockDump(boolean includeConfigs, boolean includeTemplates, boolean includeLevelDat, boolean includeLog, boolean includeCrashReport, boolean includeSkyblockBuilderWorldData) {
        PacketDistributor.sendToServer(new CreateSkyblockDumpHandler.Message(includeConfigs, includeTemplates, includeLevelDat, includeLog, includeCrashReport, includeSkyblockBuilderWorldData));
    }

    public void saveStructure(ItemStack stack, String name, boolean saveToConfig, boolean ignoreAir, boolean asSnbt, boolean netherValidation) {
        PacketDistributor.sendToServer(new SaveStructureHandler.Message(stack, name, saveToConfig, ignoreAir, asSnbt, netherValidation));
    }

    public void updateProfiles(Player player) {
        if (player.getCommandSenderWorld().isClientSide) {
            return;
        }

        this.sendProfilesInBatches((ServerPlayer) player, RandomUtility.getGameProfiles((ServerLevel) player.getCommandSenderWorld()));
    }

    public void updateProfiles(Level level) {
        if (level.isClientSide) {
            return;
        }

        Set<GameProfile> gameProfiles = RandomUtility.getGameProfiles((ServerLevel) level);
        this.sendProfilesInBatches(null, gameProfiles);
    }

    public void updateTemplateNames(Player player, List<String> names) {
        if (player.level().isClientSide) {
            return;
        }

        PacketDistributor.sendToPlayer((ServerPlayer) player, new UpdateTemplateNamesHandler.Message(names));
    }

    public void updateTemplateNames(List<String> names) {
        PacketDistributor.sendToAllPlayers(new UpdateTemplateNamesHandler.Message(names));
    }

    private void sendProfilesInBatches(ServerPlayer player, Set<GameProfile> allGameProfiles) {
        final int batchSize = 1000;
        Set<GameProfile> currentBatch = new HashSet<>(batchSize);

        for (GameProfile profile : allGameProfiles) {
            currentBatch.add(profile);

            if (currentBatch.size() == batchSize) {
                this.sendProfileBatch(player, currentBatch);
                currentBatch.clear();
            }
        }

        // send any remaining profiles in the last batch
        if (!currentBatch.isEmpty()) {
            this.sendProfileBatch(player, currentBatch);
        }
    }

    private void sendProfileBatch(ServerPlayer player, Set<GameProfile> profiles) {
        ProfilesUpdateHandler.Message msg = new ProfilesUpdateHandler.Message(profiles);
        if (player == null) {
            PacketDistributor.sendToAllPlayers(msg);
        } else {
            PacketDistributor.sendToPlayer(player, msg);
        }
    }
}
