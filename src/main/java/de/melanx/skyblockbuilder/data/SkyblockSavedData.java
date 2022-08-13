package de.melanx.skyblockbuilder.data;

import com.google.common.collect.*;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import de.melanx.skyblockbuilder.config.StartingInventory;
import de.melanx.skyblockbuilder.config.TemplateConfig;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.Spiral;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.util.BlockSnapshot;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.annotation.meta.RemoveIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/*
 * Credits go to Botania authors
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/common/world/SkyblockSavedData.java
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class SkyblockSavedData extends SavedData {

    private static final String NAME = "skyblock_builder";
    private static SkyblockSavedData clientInstance;
    public static final UUID SPAWN_ID = Util.NIL_UUID;

    private ServerLevel level;
    private Map<UUID, SkyMeta> metaInfo = Maps.newHashMap();
    private Map<UUID, Team> skyblocks = Maps.newHashMap();
    private BiMap<String, UUID> skyblockIds = HashBiMap.create();
    private BiMap<UUID, IslandPos> skyblockPositions = HashBiMap.create();
    private Spiral spiral = new Spiral();

    public static SkyblockSavedData get(Level level) {
        if (!level.isClientSide) {
            MinecraftServer server = ((ServerLevel) level).getServer();

            DimensionDataStorage storage = server.overworld().getDataStorage();
            SkyblockSavedData data = storage.computeIfAbsent(nbt -> new SkyblockSavedData().load(nbt), SkyblockSavedData::new, NAME);
            data.level = WorldUtil.getConfiguredLevel(server);
            data.getOrCreateMetaInfo(Util.NIL_UUID);
            return data;
        } else {
            return clientInstance == null ? new SkyblockSavedData() : clientInstance;
        }
    }

    public static void updateClient(SkyblockSavedData data) {
        clientInstance = data;
    }

    public Team getSpawn() {
        if (this.skyblocks.get(SPAWN_ID) != null) {
            return this.skyblocks.get(SPAWN_ID);
        }

        SkyblockBuilder.getLogger().info("Successfully generated spawn.");
        Team team = this.createTeam("Spawn", TemplateConfig.spawn.flatMap(templateInfo -> Optional.of(new ConfiguredTemplate(templateInfo))).orElse(TemplateData.get(this.level).getConfiguredTemplate()));
        //noinspection ConstantConditions
        team.addPlayer(Util.NIL_UUID);

        this.setDirty();
        return team;
    }

    public Optional<Team> getSpawnOption() {
        return Optional.ofNullable(this.skyblocks.get(SPAWN_ID));
    }

    public Pair<IslandPos, Team> create(String teamName, ConfiguredTemplate template) {
        IslandPos islandPos;
        Team team;
        if (teamName.equalsIgnoreCase("spawn")) {
            islandPos = new IslandPos(this.level, 0, 0);
            team = new Team(this, islandPos, SPAWN_ID, template.getDirection());
        } else {
            do {
                int[] pos = this.spiral.next();
                islandPos = new IslandPos(this.level, pos[0], pos[1]);
            } while (this.skyblockPositions.containsValue(islandPos));
            team = new Team(this, islandPos, template.getDirection());
        }

        Set<BlockPos> positions = initialPossibleSpawns(islandPos.getCenter(), template);

        team.setPossibleSpawns(positions);
        team.setName(teamName);

        this.skyblocks.put(team.getId(), team);
        this.skyblockIds.put(team.getName().toLowerCase(Locale.ROOT), team.getId());
        this.skyblockPositions.put(team.getId(), islandPos);

        this.setDirty();
        return Pair.of(islandPos, team);
    }

    public SkyblockSavedData load(CompoundTag nbt) {
        Map<UUID, SkyMeta> metaInfo = Maps.newHashMap();
        Map<UUID, Team> skyblocks = Maps.newHashMap();
        BiMap<String, UUID> skyblockIds = HashBiMap.create();
        BiMap<UUID, IslandPos> skyblockPositions = HashBiMap.create();
        for (Tag inbt : nbt.getList("Islands", Tag.TAG_COMPOUND)) {
            CompoundTag tag = (CompoundTag) inbt;

            IslandPos island = IslandPos.fromTag(tag.getCompound("Island"));
            Team team = Team.create(this, tag);

            skyblocks.put(team.getId(), team);
            skyblockIds.put(team.getName().toLowerCase(Locale.ROOT), team.getId());
            skyblockPositions.put(team.getId(), island);
        }

        for (Tag inbt : nbt.getList("MetaInformation", Tag.TAG_COMPOUND)) {
            CompoundTag tag = (CompoundTag) inbt;

            UUID player = tag.getUUID("Player");
            SkyMeta meta = SkyMeta.get(this, tag.getCompound("Meta"));
            metaInfo.put(player, meta);
        }
        this.metaInfo = metaInfo;
        this.skyblocks = skyblocks;
        this.skyblockIds = skyblockIds;
        this.skyblockPositions = skyblockPositions;
        this.spiral = Spiral.fromArray(nbt.getIntArray("SpiralState"));

        return this;
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound) {
        ListTag islands = new ListTag();
        for (Team team : this.skyblocks.values()) {
            islands.add(team.serializeNBT());
        }

        ListTag metaInfo = new ListTag();
        for (Map.Entry<UUID, SkyMeta> entry : this.metaInfo.entrySet()) {
            SkyMeta meta = entry.getValue();
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("Player", entry.getKey());
            entryTag.put("Meta", meta.save());

            metaInfo.add(entryTag);
        }

        compound.putIntArray("SpiralState", this.spiral.toIntArray());
        compound.put("Islands", islands);
        compound.put("MetaInformation", metaInfo);
        return compound;
    }

    @Nullable
    public IslandPos getTeamIsland(UUID teamId) {
        return this.skyblockPositions.get(teamId);
    }

    public boolean hasPlayerTeam(Player player) {
        return this.hasPlayerTeam(player.getGameProfile().getId());
    }

    public boolean hasPlayerTeam(UUID player) {
        Team team = this.getTeamFromPlayer(player);
        return team != null && !team.isSpawn();
    }

    public boolean addPlayerToTeam(UUID teamId, Player player) {
        return this.addPlayerToTeam(teamId, player.getGameProfile().getId());
    }

    public boolean addPlayerToTeam(UUID teamId, UUID playerId) {
        Team team = this.skyblocks.get(teamId);

        if (team != null) {
            return team.addPlayer(playerId);
        }

        return false;
    }

    public boolean addPlayerToTeam(String teamName, Player player) {
        return this.addPlayerToTeam(teamName, player.getGameProfile().getId());
    }

    public boolean addPlayerToTeam(String teamName, UUID player) {
        UUID teamId = this.skyblockIds.get(teamName.toLowerCase(Locale.ROOT));

        return this.addPlayerToTeam(teamId, player);
    }

    public boolean addPlayerToTeam(Team team, Player player) {
        return this.addPlayerToTeam(team, player.getGameProfile().getId());
    }

    public boolean addPlayerToTeam(Team team, UUID player) {
        if (!team.isSpawn()) {
            team.broadcast(Component.translatable("skyblockbuilder.event.player_joined", GameProfileCache.getName(player)), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        }

        ServerLevel level = team.getLevel();
        if (level != null && !this.metaInfo.get(player).getPreviousTeamIds().contains(team.getId())) {
            ServerPlayer onlinePlayer = level.getServer().getPlayerList().getPlayer(player);
            if (onlinePlayer != null) {
                StartingInventory.getStarterItems().forEach(entry -> {
                    if (entry.getLeft() == EquipmentSlot.MAINHAND) {
                        onlinePlayer.getInventory().add(entry.getRight().copy());
                    } else {
                        onlinePlayer.setItemSlot(entry.getLeft(), entry.getRight().copy());
                    }
                });
            }
        }

        this.getSpawn().removePlayer(player);
        team.addPlayer(player);
        this.setDirty();
        return true;
    }

    @Nullable
    public Team createTeam(String teamName) {
        if (this.level == null) {
            return null;
        }

        return this.createTeam(teamName, TemplateData.get(this.level).getConfiguredTemplate());
    }

    @Nullable
    public Team createTeam(String teamName, ConfiguredTemplate template) {
        if (this.teamExists(teamName) || this.level == null) {
            return null;
        }

        Pair<IslandPos, Team> pair = this.create(teamName, template);
        Team team = pair.getRight();
        List<BlockPos> possibleSpawns = new ArrayList<>(this.getPossibleSpawns(team.getIsland(), template));
        team.setPossibleSpawns(possibleSpawns);
        team.setDirection(template.getDirection());

        StructurePlaceSettings settings = new StructurePlaceSettings().setKnownShape(true);
        BlockPos center = team.getIsland().getCenter();
        //noinspection unchecked
        List<BlockSnapshot> capturedBlockSnapshots = (List<BlockSnapshot>) this.level.capturedBlockSnapshots.clone();
        this.level.capturedBlockSnapshots.clear();
        this.level.captureBlockSnapshots = true;
        template.getTemplate().placeInWorld(this.level, center, center, settings, RandomSource.create(), Block.UPDATE_CLIENTS);
        this.level.captureBlockSnapshots = false;
        this.level.capturedBlockSnapshots.addAll(capturedBlockSnapshots);

        this.skyblocks.put(team.getId(), team);
        this.skyblockIds.put(team.getName().toLowerCase(Locale.ROOT), team.getId());
        this.skyblockPositions.put(team.getId(), team.getIsland());

        this.setDirty();
        return team;
    }

    @Nullable
    public Team createTeamAndJoin(String teamName, Player player) {
        return this.createTeamAndJoin(teamName, player.getGameProfile().getId());
    }

    @Nullable
    public Team createTeamAndJoin(String teamName, UUID player) {
        Team team = this.createTeam(teamName);
        if (team == null) return null;

        team.addPlayer(player);
        this.setDirty();
        return team;
    }

    public boolean removePlayerFromTeam(Player player) {
        return this.removePlayerFromTeam(player.getGameProfile().getId());
    }

    public boolean removePlayerFromTeam(UUID player) {
        for (Map.Entry<UUID, Team> entry : this.skyblocks.entrySet()) {
            Team team = entry.getValue();
            if (team.isSpawn()) continue;
            if (team.hasPlayer(player)) {
                boolean removed = team.removePlayer(player);
                if (removed) {
                    team.broadcast(Component.translatable("skyblockbuilder.event.remove_player", GameProfileCache.getName(player)), Style.EMPTY.applyFormat(ChatFormatting.RED));
                    //noinspection ConstantConditions
                    this.getTeam(SPAWN_ID).addPlayer(player);
                    this.getOrCreateMetaInfo(player).setTeamId(SPAWN_ID);
                }
                return removed;
            }
        }
        return false;
    }

    public void removeAllPlayersFromTeam(@Nonnull Team team) {
        Set<UUID> players = Sets.newHashSet(team.getPlayers());
        team.removeAllPlayers();
        Team spawn = this.getSpawn();
        for (UUID player : players) {
            this.addPlayerToTeam(spawn, player);
        }
        this.setDirty();
    }

    @Nullable
    public Team getTeam(String name) {
        return this.getTeam(this.skyblockIds.get(name.toLowerCase(Locale.ROOT)));
    }

    @Nullable
    public Team getTeam(UUID teamId) {
        return this.skyblocks.get(teamId);
    }

    public boolean deleteTeam(String team) {
        UUID teamId = this.skyblockIds.get(team.toLowerCase(Locale.ROOT));

        return this.deleteTeam(teamId);
    }

    public boolean deleteTeam(UUID teamId) {
        Team removedTeam = this.skyblocks.remove(teamId);

        if (removedTeam == null) {
            return false;
        }

        this.skyblockIds.inverse().remove(teamId);
        this.skyblockPositions.inverse().remove(removedTeam.getIsland());
        this.skyblocks.get(SPAWN_ID).addPlayers(removedTeam.getPlayers());

        return true;
    }

    @Nullable
    public Team getTeamFromPlayer(Player player) {
        return this.getTeamFromPlayer(player.getGameProfile().getId());
    }

    @Nullable
    public Team getTeamFromPlayer(UUID player) {
        SkyMeta meta = this.metaInfo.get(player);

        if (meta == null) {
            return null;
        }

        Team team = this.skyblocks.getOrDefault(meta.getTeamId(), this.skyblocks.get(SPAWN_ID));

        return team == null || team.isSpawn() ? null : team;
    }

    public boolean teamExists(String name) {
        return this.skyblockIds.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public boolean teamExists(UUID teamId) {
        return this.skyblocks.containsKey(teamId);
    }

    public Collection<Team> getTeams() {
        return this.skyblocks.values();
    }

    public void addInvite(Team team, Player invitor, Player player) {
        this.addInvite(team, invitor, player.getGameProfile().getId());
    }

    public void addInvite(Team team, Player invitor, UUID id) {
        SkyMeta meta = this.metaInfo.get(id);

        if (!meta.getInvites().contains(team.getId())) {
            meta.addInvite(team.getId());
            team.broadcast(Component.translatable("skyblockbuilder.event.invite_player", invitor.getDisplayName(), GameProfileCache.getName(id)), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        }

        this.setDirty();
    }

    public boolean hasInvites(Player player) {
        return this.hasInvites(player.getGameProfile().getId());
    }

    public boolean hasInvites(UUID player) {
        SkyMeta meta = this.metaInfo.get(player);
        return meta != null && !meta.getInvites().isEmpty();
    }

    public boolean hasInviteFrom(Team team, Player player) {
        return this.hasInviteFrom(team, player.getGameProfile().getId());
    }

    public boolean hasInviteFrom(Team team, UUID player) {
        SkyMeta meta = this.metaInfo.get(player);

        return meta != null && meta.getInvites().contains(team.getId());
    }

    public List<UUID> getInvites(Player player) {
        return this.getInvites(player.getGameProfile().getId());
    }

    public List<UUID> getInvites(UUID player) {
        SkyMeta meta = this.metaInfo.get(player);
        return meta == null ? Lists.newArrayList() : meta.getInvites();
    }

    public boolean acceptInvite(Team team, Player player) {
        return this.acceptInvite(team, player.getGameProfile().getId());
    }

    public boolean acceptInvite(Team team, UUID id) {
        SkyMeta meta = this.metaInfo.get(id);

        if (meta == null) {
            return false;
        }

        if (meta.getInvites().contains(team.getId())) {
            team.broadcast(Component.translatable("skyblockbuilder.event.accept_invite", GameProfileCache.getName(id)), Style.EMPTY.applyFormat(ChatFormatting.GOLD));

            this.addPlayerToTeam(team.getName(), id);
            meta.resetInvites();
            //noinspection ConstantConditions
            WorldUtil.teleportToIsland(this.level.getServer().getPlayerList().getPlayer(id), team);
            this.setDirty();

            return true;
        }

        return false;
    }

    public boolean declineInvite(Team team, Player player) {
        return this.declineInvite(team, player.getGameProfile().getId());
    }

    public boolean declineInvite(Team team, UUID id) {
        SkyMeta meta = this.metaInfo.get(id);

        if (meta == null) {
            return false;
        }

        meta.removeInvite(team.getId());
        this.setDirty();
        return true;
    }

    public void renameTeam(Team team, @Nullable ServerPlayer player, String name) {
        String oldName = team.getName().toLowerCase();
        this.skyblockIds.remove(oldName);

        team.setName(name);
        this.skyblockIds.put(name.toLowerCase(Locale.ROOT), team.getId());

        Component playerName = player != null ? player.getDisplayName() : Component.literal("Server");

        team.broadcast(Component.translatable("skyblockbuilder.event.rename_team", playerName, oldName, name), Style.EMPTY.applyFormat(ChatFormatting.DARK_RED));

        this.setDirty();
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public SkyMeta getMetaInfo(Player player) {
        return this.getOrCreateMetaInfo(player);
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public SkyMeta getMetaInfo(UUID id) {
        return this.getOrCreateMetaInfo(id);
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public SkyMeta addMetaInfo(Player player) {
        return this.getOrCreateMetaInfo(player);
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public SkyMeta addMetaInfo(UUID id) {
        return this.getOrCreateMetaInfo(id);
    }

    public SkyMeta getOrCreateMetaInfo(Player player) {
        return this.getOrCreateMetaInfo(player.getGameProfile().getId());
    }

    public SkyMeta getOrCreateMetaInfo(UUID id) {
        return this.metaInfo.computeIfAbsent(id, meta -> new SkyMeta(this, id));
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public Set<BlockPos> getPossibleSpawns(IslandPos pos) {
        return this.getPossibleSpawns(pos, TemplateLoader.getConfiguredTemplate());
    }

    public Set<BlockPos> getPossibleSpawns(IslandPos pos, ConfiguredTemplate template) {
        if (!this.skyblockPositions.containsValue(pos)) {
            return initialPossibleSpawns(pos.getCenter(), template);
        }

        return this.skyblocks.get(this.skyblockPositions.inverse().get(pos)).getPossibleSpawns();
    }

    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public static Set<BlockPos> initialPossibleSpawns(BlockPos center) {
        return initialPossibleSpawns(center, TemplateLoader.getConfiguredTemplate());
    }

    public static Set<BlockPos> initialPossibleSpawns(BlockPos center, ConfiguredTemplate template) {
        Set<BlockPos> positions = Sets.newHashSet();
        for (BlockPos pos : template.getDefaultSpawns()) {
            positions.add(center.offset(pos.immutable()));
        }

        return positions;
    }

    @Override
    public void setDirty() {
        super.setDirty();
        if (this.level != null) {
            SkyblockBuilder.getNetwork().updateData(this.level, this);
        }
    }

    public void setDirtySilently() {
        super.setDirty();
    }

    @Nullable
    public ServerLevel getLevel() {
        return this.level;
    }
}
