package de.melanx.skyblockbuilder.data;

import com.google.common.collect.*;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import de.melanx.skyblockbuilder.compat.CadmusCompat;
import de.melanx.skyblockbuilder.config.common.SpawnConfig;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.values.TemplateSurroundingBlocks;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.Spiral;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.fml.ModList;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
 * Credits go to Botania authors
 * https://github.com/VazkiiMods/Botania/blob/1.16.x-forge/src/main/java/vazkii/botania/common/world/SkyblockSavedData.java
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class SkyblockSavedData extends SavedData {

    private static final String NAME = "skyblockbuilder/main";
    private static SkyblockSavedData clientInstance;
    public static final UUID SPAWN_ID = Util.NIL_UUID;

    private ServerLevel level;
    private ConcurrentMap<UUID, SkyMeta> metaInfo = new ConcurrentHashMap<>();
    private ConcurrentMap<UUID, Team> skyblocks = new ConcurrentHashMap<>();
    private BiMap<String, UUID> skyblockIds = Maps.synchronizedBiMap(HashBiMap.create());
    private BiMap<UUID, IslandPos> skyblockPositions = Maps.synchronizedBiMap(HashBiMap.create());
    private Spiral spiral = new Spiral();

    public static SavedData.Factory<SkyblockSavedData> factory() {
        return new SavedData.Factory<>(SkyblockSavedData::new, (nbt, provider) -> SkyblockSavedData.load(nbt));
    }

    public static SkyblockSavedData get(Level level) {
        if (!level.isClientSide) {
            MinecraftServer server = ((ServerLevel) level).getServer();

            DimensionDataStorage storage = server.overworld().getDataStorage();
            SkyblockSavedData data = storage.computeIfAbsent(SkyblockSavedData.factory(), NAME);
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
        Team team = this.createTeam("Spawn", TemplatesConfig.spawn.flatMap(templateInfo -> Optional.of(new ConfiguredTemplate(templateInfo))).orElse(TemplateData.get(this.level).getConfiguredTemplate()));
        //noinspection ConstantConditions
        team.addPlayer(Util.NIL_UUID);

        if (ModList.get().isLoaded(CadmusCompat.MODID)) {
            CadmusCompat.protectSpawn(this.level, team);
        }

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
            int[] pos = new int[]{0, 0};
            if (SpawnConfig.skipCenterIslandCreation) {
                pos = this.spiral.next();
            }
            islandPos = new IslandPos(this.level, pos[0], pos[1], template);
            team = new Team(this, islandPos, SPAWN_ID);
        } else {
            do {
                int[] pos = this.spiral.next();
                islandPos = new IslandPos(this.level, pos[0], pos[1], template);
            } while (this.skyblockPositions.containsValue(islandPos));
            team = new Team(this, islandPos);
        }

        Set<TemplatesConfig.Spawn> positions = initialPossibleSpawns(islandPos.getCenter(), template);

        team.setPossibleSpawns(positions);
        team.setName(teamName);

        this.skyblocks.put(team.getId(), team);
        this.skyblockIds.put(team.getName().toLowerCase(Locale.ROOT), team.getId());
        this.skyblockPositions.put(team.getId(), islandPos);

        this.setDirty();
        return Pair.of(islandPos, team);
    }

    public static SkyblockSavedData load(CompoundTag nbt) {
        SkyblockSavedData data = new SkyblockSavedData();
        ConcurrentMap<UUID, SkyMeta> metaInfo = new ConcurrentHashMap<>();
        ConcurrentMap<UUID, Team> skyblocks = new ConcurrentHashMap<>();
        BiMap<String, UUID> skyblockIds = Maps.synchronizedBiMap(HashBiMap.create());
        BiMap<UUID, IslandPos> skyblockPositions = Maps.synchronizedBiMap(HashBiMap.create());
        for (Tag inbt : nbt.getList("Islands", Tag.TAG_COMPOUND)) {
            CompoundTag tag = (CompoundTag) inbt;

            IslandPos island = IslandPos.fromTag(tag.getCompound("Island"));
            Team team = Team.create(data, tag);

            skyblocks.put(team.getId(), team);
            skyblockIds.put(team.getName().toLowerCase(Locale.ROOT), team.getId());
            skyblockPositions.put(team.getId(), island);
        }

        for (Tag inbt : nbt.getList("MetaInformation", Tag.TAG_COMPOUND)) {
            CompoundTag tag = (CompoundTag) inbt;

            UUID player = tag.getUUID("Player");
            SkyMeta meta = SkyMeta.get(data, tag.getCompound("Meta"));
            metaInfo.put(player, meta);
        }
        data.metaInfo = metaInfo;
        data.skyblocks = skyblocks;
        data.skyblockIds = skyblockIds;
        data.skyblockPositions = skyblockPositions;
        data.spiral = Spiral.fromArray(nbt.getIntArray("SpiralState"));

        return data;
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider registries) {
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
        if (level != null && !this.getOrCreateMetaInfo(player).getPreviousTeamIds().contains(team.getId())) {
            ServerPlayer onlinePlayer = level.getServer().getPlayerList().getPlayer(player);
            if (onlinePlayer != null && (TemplatesConfig.spawn.isEmpty() || !team.isSpawn())) {
                RandomUtility.setStartInventory(onlinePlayer);
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
        List<TemplatesConfig.Spawn> possibleSpawns = new ArrayList<>(this.getPossibleSpawns(team.getIsland(), template));
        team.setPossibleSpawns(possibleSpawns);

        BlockPos center = team.getIsland().getCenter();
        template.placeInWorld(this.level, team, TemplateLoader.STRUCTURE_PLACE_SETTINGS, RandomSource.create(), Block.UPDATE_CLIENTS);
        SkyblockSavedData.surround(this.level, center, template);

        this.skyblocks.put(team.getId(), team);
        this.skyblockIds.put(team.getName().toLowerCase(Locale.ROOT), team.getId());
        this.skyblockPositions.put(team.getId(), team.getIsland());

        SkyblockBuilder.getLogger().info("Created team {} ({}) at {} with template {}", team.getName(), team.getId(), center, template.getName());
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
        SkyMeta meta = this.getOrCreateMetaInfo(id);

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

    public SkyMeta getOrCreateMetaInfo(Player player) {
        return this.getOrCreateMetaInfo(player.getGameProfile().getId());
    }

    public SkyMeta getOrCreateMetaInfo(UUID id) {
        return this.metaInfo.computeIfAbsent(id, meta -> new SkyMeta(this, id));
    }

    public Set<TemplatesConfig.Spawn> getPossibleSpawns(IslandPos pos, ConfiguredTemplate template) {
        if (!this.skyblockPositions.containsValue(pos)) {
            return initialPossibleSpawns(pos.getCenter(), template);
        }

        return this.skyblocks.get(this.skyblockPositions.inverse().get(pos)).getPossibleSpawns();
    }

    public static Set<TemplatesConfig.Spawn> initialPossibleSpawns(BlockPos center, ConfiguredTemplate template) {
        Set<TemplatesConfig.Spawn> positions = new HashSet<>();
        for (TemplatesConfig.Spawn spawn : template.getDefaultSpawns()) {
            positions.add(new TemplatesConfig.Spawn(center.offset(spawn.pos().immutable()), spawn.direction()));
        }

        return positions;
    }

    public static void surround(ServerLevel level, BlockPos zero, ConfiguredTemplate configuredTemplate) {
        if (configuredTemplate.getSurroundingBlocks().isEmpty() || configuredTemplate.getSurroundingMargin() <= 0) {
            return;
        }

        StructureTemplate template = configuredTemplate.getTemplate();
        BoundingBox box = new BoundingBox(zero.getX(), zero.getY(), zero.getZ(),
                zero.getX() + template.size.getX() - 1, zero.getY() + template.size.getY() - 1, zero.getZ() + template.size.getZ() - 1);
        BoundingBox outside = box.inflatedBy(configuredTemplate.getSurroundingMargin());
        RandomSource random = RandomSource.create();
        BlockPos.betweenClosedStream(outside).forEach(blockPos -> {
            if (!box.isInside(blockPos)) {
                Optional<TemplateSurroundingBlocks.WeightedBlock> optional = configuredTemplate.getSurroundingBlocks().getRandom(random);
                optional.ifPresent(weightedBlock -> level.setBlock(blockPos, weightedBlock.block().defaultBlockState(), Block.UPDATE_CLIENTS));
            }
        });
    }

    @Override
    public void setDirty() {
        super.setDirty();
        if (this.level != null) {
            SkyblockBuilder.getNetwork().updateData(this.level, this);
            for (ServerPlayer player : this.level.getServer().getPlayerList().getPlayers()) {
                player.refreshTabListName();
            }
        }
    }

    public void setDirtySilently() {
        super.setDirty();
    }

    @Override
    public void save(@Nonnull File file, @Nonnull HolderLookup.Provider registries) {
        if (this.isDirty()) {
            try {
                Files.createDirectories(file.toPath().getParent());
            } catch (IOException e) {
                SkyblockBuilder.getLogger().error("Could not create directory: {}", file.getAbsolutePath(), e);
            }
        }

        super.save(file, registries);
    }

    @Nullable
    public ServerLevel getLevel() {
        return this.level;
    }
}
