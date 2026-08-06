package de.melanx.skyblockbuilder.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.client.GameProfileCache;
import de.melanx.skyblockbuilder.compat.CadmusCompat;
import de.melanx.skyblockbuilder.compat.infiniverse.InfiniverseCompat;
import de.melanx.skyblockbuilder.config.common.InventoryConfig;
import de.melanx.skyblockbuilder.config.common.SpawnConfig;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.config.common.WorldConfig;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.util.*;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.neoforged.fml.ModList;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
 * Credits go to Botania authors
 * https://github.com/VazkiiMods/Botania/blob/1.16.x-forge/src/main/java/vazkii/botania/common/world/SkyblockSavedData.java
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class SkyblockSavedData extends SavedData {

    public static final String ISLANDS = "islands";
    public static final String META_INFO = "meta_information";
    public static final String SPIRAL_STATE = "spiral_state";
    public static final String MULTI_DIMENSIONAL = "multi_dimensional";

    private static final Identifier ID = SkyblockBuilder.getInstance().id("skyblockbuilder/main");
    private static SkyblockSavedData clientInstance;
    public static final UUID SPAWN_ID = Util.NIL_UUID;

    protected final TeamRegistry registry = new TeamRegistry();
    protected ConcurrentMap<UUID, SkyMeta> metaInfo = new ConcurrentHashMap<>();

    protected abstract IslandPos nextIslandPos(ConfiguredTemplate template);

    protected abstract IslandPos nextSpawnPos(ConfiguredTemplate template);

    protected abstract void onTeamCreated(Team team, ConfiguredTemplate template);

    protected abstract void onTeamDeleted(Team team);

    @Nullable
    public abstract ServerLevel getLevelFor(Team team);

    public abstract void restoreInfiniverseDimensions(MinecraftServer server);

    public ServerLevel getLevel() {
        return this.getLevelFor(null);
    }

    public static SavedDataType<SkyblockSavedData> type() {
        return new SavedDataType<>(ID,
                level -> InfiniverseCompat.useInfiniverse()
                        ? new MultiWorldImpl(level != null ? level.getServer() : null)
                        : new SingleWorldImpl(level),
                SkyblockSavedData::makeCodec
        );
    }

    public static Codec<SkyblockSavedData> makeCodec(@Nullable ServerLevel level) {
        return makeCodec(level, null);
    }

    /**
     * @param metaFilter when non-null, only this player's {@link SkyMeta} is encoded. Used for the
     *                   client sync packet so a player never receives everyone else's invites.
     */
    public static Codec<SkyblockSavedData> makeCodec(@Nullable ServerLevel level, @Nullable UUID metaFilter) {
        MapCodec<SingleWorldImpl> single = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Team.CODEC.listOf().optionalFieldOf(ISLANDS, List.of()).forGetter(SkyblockSavedData::teams),
                SkyMeta.CODEC.listOf().optionalFieldOf(META_INFO, List.of()).forGetter(data -> data.metas(metaFilter)),
                Spiral.CODEC.optionalFieldOf(SPIRAL_STATE).forGetter(data -> Optional.of(data.spiral))
        ).apply(instance, (teams, metas, spiral) ->
                new SingleWorldImpl(level, teams, metas, spiral.orElseGet(Spiral::new))));

        MapCodec<MultiWorldImpl> multi = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Team.CODEC.listOf().optionalFieldOf(ISLANDS, List.of()).forGetter(SkyblockSavedData::teams),
                SkyMeta.CODEC.listOf().optionalFieldOf(META_INFO, List.of()).forGetter(data -> data.metas(metaFilter))
        ).apply(instance, (teams, metas) ->
                new MultiWorldImpl(level != null ? level.getServer() : null, teams, metas)));

        return Codec.BOOL.dispatch(MULTI_DIMENSIONAL,
                data -> data instanceof MultiWorldImpl,
                flag -> flag ? multi : single);
    }

    protected List<Team> teams() {
        return List.copyOf(this.registry.all());
    }

    protected List<SkyMeta> metas(@Nullable UUID filter) {
        return this.metaInfo.values().stream()
                .filter(meta -> filter == null || filter.equals(meta.getOwner()))
                .toList();
    }

    /**
     * Teams and metas are decoded before this instance exists, so they arrive unbound.
     */
    protected void loadInto(List<Team> teams, List<SkyMeta> metas) {
        for (Team team : teams) {
            team.bindData(this);
            this.registry.add(team);
        }

        for (SkyMeta meta : metas) {
            meta.bindData(this);
            this.metaInfo.put(meta.getOwner(), meta);
        }
    }

    public static SkyblockSavedData get(Level level) {
        if (!level.isClientSide()) {
            MinecraftServer server = ((ServerLevel) level).getServer();

            SavedDataStorage storage = server.overworld().getDataStorage();
            SkyblockSavedData data = storage.computeIfAbsent(SkyblockSavedData.type());
            if (data instanceof MultiWorldImpl multi) {
                multi.server = server;
            } else if (data instanceof SingleWorldImpl single) {
                single.level = WorldUtil.getConfiguredLevel(server);
            }
            data.getOrCreateMetaInfo(Util.NIL_UUID);
            return data;
        } else {
            return clientInstance == null ? new SingleWorldImpl(null) : clientInstance;
        }
    }

    public static void updateClient(SkyblockSavedData data) {
        clientInstance = data;
    }

    public Team getSpawn() {
        if (this.registry.getById(SPAWN_ID) != null) {
            return this.registry.getById(SPAWN_ID);
        }

        SkyblockBuilder.getLogger().info("Successfully generated spawn.");
        ServerLevel level = this.getLevel();
        Team team = this.createTeam("Spawn", TemplatesConfig.mainSpawnIsland.flatMap(templateInfo -> Optional.of(new ConfiguredTemplate(templateInfo))).orElse(TemplateData.get(level).getConfiguredTemplate()));
        //noinspection ConstantConditions
        team.addPlayer(Util.NIL_UUID);

        if (ModList.get().isLoaded(CadmusCompat.MODID)) {
            CadmusCompat.protectSpawn(level, team);
        }

        this.setDirty();
        return team;
    }

    public Optional<Team> getSpawnOption() {
        return Optional.ofNullable(this.registry.getById(SPAWN_ID));
    }

    public Pair<IslandPos, Team> create(String teamName, ConfiguredTemplate template) {
        Team team = this.createTeamInternally(teamName, template);
        this.onTeamCreated(team, template);
        IslandPos islandPos = team.getIsland();

        Set<TemplatesConfig.Spawn> positions = initialPossibleSpawns(islandPos.getCenter(), template);

        team.setPossibleSpawns(positions);
        team.setName(teamName);

        this.registry.add(team);

        this.setDirty();
        return Pair.of(islandPos, team);
    }

    private Team createTeamInternally(String teamName, ConfiguredTemplate template) {
        IslandPos islandPos;
        boolean isSpawn = teamName.equalsIgnoreCase("spawn");

        if (isSpawn) {
            return new Team(this, this.nextSpawnPos(template), SPAWN_ID);
        }

        return new Team(this, this.nextIslandPos(template));
    }

    @Nullable
    public IslandPos getTeamIsland(UUID teamId) {
        Team team = this.registry.getById(teamId);
        return team != null ? team.getIsland() : null;
    }

    public boolean hasPlayerTeam(Player player) {
        return this.hasPlayerTeam(player.getGameProfile().id());
    }

    public boolean hasPlayerTeam(UUID player) {
        Team team = this.getTeamFromPlayer(player);
        return team != null && !team.isSpawn();
    }

    public boolean addPlayerToTeam(UUID teamId, Player player) {
        return this.addPlayerToTeam(teamId, player.getGameProfile().id());
    }

    public boolean addPlayerToTeam(UUID teamId, UUID playerId) {
        Team team = this.registry.getById(teamId);

        if (team != null) {
            return team.addPlayer(playerId);
        }

        return false;
    }

    public boolean addPlayerToTeam(String teamName, Player player) {
        return this.addPlayerToTeam(teamName, player.getGameProfile().id());
    }

    public boolean addPlayerToTeam(String teamName, UUID player) {
        Team team = this.registry.getByName(teamName);
        if (team == null) return false;

        return this.addPlayerToTeam(team.id(), player);
    }

    public boolean addPlayerToTeam(Team team, Player player) {
        return this.addPlayerToTeam(team, player.getGameProfile().id());
    }

    public boolean addPlayerToTeam(Team team, UUID player) {
        if (!team.isSpawn()) {
            team.broadcast(SkyComponents.EVENT_PLAYER_JOINED.apply(GameProfileCache.getName(player)), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        }

        ServerLevel level = team.getLevel();
        if (level != null
                && (InventoryConfig.initialInventoryType == InventoryConfig.InitialInventoryType.SPAWN) == team.isSpawn()
                && !this.getOrCreateMetaInfo(player).getPreviousTeamIds().contains(team.id())) {
            ServerPlayer onlinePlayer = level.getServer().getPlayerList().getPlayer(player);
            if (onlinePlayer != null) {
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
        ServerLevel level = this.getLevel();
        if (level == null) {
            return null;
        }

        if (teamName.length() > Team.MAX_NAME_LENGTH) {
            return null;
        }

        return this.createTeam(teamName, TemplateData.get(level).getConfiguredTemplate());
    }

    @Nullable
    public Team createTeam(String teamName, ConfiguredTemplate template) {
        if (this.teamExists(teamName) || this.getLevel() == null) {
            return null;
        }

        Pair<IslandPos, Team> pair = this.create(teamName, template);
        Team team = pair.getRight();
        List<TemplatesConfig.Spawn> possibleSpawns = new ArrayList<>(this.getPossibleSpawns(team.getIsland(), template));
        team.setPossibleSpawns(possibleSpawns);

        BlockPos center = team.getIsland().getCenter();

        ServerLevel teamLevel = this.getLevelFor(team);

        template.placeInWorld(teamLevel, team, TemplateUtil.STRUCTURE_PLACE_SETTINGS, RandomSource.create(), Block.UPDATE_CLIENTS);
        SkyblockSavedData.surround(teamLevel, center, template);

        // team was already added to registry in create(); no need to add again

        SkyblockBuilder.getLogger().info("Created team {} ({}) at {} with template {}", team.getName(), team.id(), center, template.getName());
        this.setDirty();
        return team;
    }

    @Nullable
    public Team createTeamAndJoin(String teamName, Player player) {
        return this.createTeamAndJoin(teamName, player.getGameProfile().id());
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
        return this.removePlayerFromTeam(player.getGameProfile().id());
    }

    public boolean removePlayerFromTeam(UUID player) {
        for (Team team : this.registry.all()) {
            if (team.isSpawn()) continue;
            if (team.hasPlayer(player)) {
                boolean removed = team.removePlayer(player);
                if (removed) {
                    team.broadcast(SkyComponents.EVENT_REMOVE_PLAYER.apply(GameProfileCache.getName(player)), Style.EMPTY.applyFormat(ChatFormatting.RED));
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
        return this.registry.getByName(name);
    }

    @Nullable
    public Team getTeam(UUID teamId) {
        return this.registry.getById(teamId);
    }

    public boolean deleteTeam(String team) {
        Team t = this.registry.getByName(team);
        if (t == null) return false;
        return this.deleteTeam(t.id());
    }

    public boolean deleteTeam(UUID teamId) {
        Team removedTeam = this.registry.getById(teamId);

        if (removedTeam == null) {
            return false;
        }

        Team spawn = this.registry.getById(SPAWN_ID);
        if (spawn != null) {
            spawn.addPlayers(removedTeam.getPlayers());
        }

        this.registry.remove(teamId);
        this.onTeamDeleted(removedTeam);

        return true;
    }

    @Nullable
    public Team getTeamFromPlayer(Player player) {
        return this.getTeamFromPlayer(player.getGameProfile().id());
    }

    @Nullable
    public Team getTeamFromPlayer(UUID player) {
        SkyMeta meta = this.metaInfo.get(player);

        if (meta == null) {
            return null;
        }

        Team team = this.registry.getById(meta.getTeamId());
        if (team == null) {
            team = this.registry.getById(SPAWN_ID);
        }

        return team == null || team.isSpawn() ? null : team;
    }

    public boolean teamExists(String name) {
        return this.registry.containsName(name);
    }

    public boolean teamExists(UUID teamId) {
        return this.registry.containsId(teamId);
    }

    public Collection<Team> getTeams() {
        return Collections.unmodifiableCollection(this.registry.all());
    }

    public void addInvite(Team team, Player invitor, Player player) {
        this.addInvite(team, invitor, player.getGameProfile().id());
    }

    public void addInvite(Team team, Player invitor, UUID id) {
        SkyMeta meta = this.getOrCreateMetaInfo(id);

        if (!meta.getInvites().contains(team.id())) {
            meta.addInvite(team.id());
            team.broadcast(SkyComponents.EVENT_INVITE_PLAYER.apply(invitor.getDisplayName(), GameProfileCache.getName(id)), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        }

        this.setDirty();
    }

    public boolean hasInvites(Player player) {
        return this.hasInvites(player.getGameProfile().id());
    }

    public boolean hasInvites(UUID player) {
        SkyMeta meta = this.metaInfo.get(player);
        return meta != null && !meta.getInvites().isEmpty();
    }

    public boolean hasInviteFrom(Team team, Player player) {
        return this.hasInviteFrom(team, player.getGameProfile().id());
    }

    public boolean hasInviteFrom(Team team, UUID player) {
        SkyMeta meta = this.metaInfo.get(player);

        return meta != null && meta.getInvites().contains(team.id());
    }

    public List<UUID> getInvites(Player player) {
        return this.getInvites(player.getGameProfile().id());
    }

    public List<UUID> getInvites(UUID player) {
        SkyMeta meta = this.metaInfo.get(player);
        return meta == null ? Lists.newArrayList() : meta.getInvites();
    }

    public boolean acceptInvite(Team team, Player player) {
        return this.acceptInvite(team, player.getGameProfile().id());
    }

    public boolean acceptInvite(Team team, UUID id) {
        SkyMeta meta = this.metaInfo.get(id);

        if (meta == null) {
            return false;
        }

        if (meta.getInvites().contains(team.id())) {
            team.broadcast(SkyComponents.EVENT_ACCEPT_INVITE.apply(GameProfileCache.getName(id)), Style.EMPTY.applyFormat(ChatFormatting.GOLD));

            this.addPlayerToTeam(team.getName(), id);
            meta.resetInvites();
            //noinspection ConstantConditions
            WorldUtil.teleportToIsland(this.getLevel().getServer().getPlayerList().getPlayer(id), team);
            this.setDirty();

            return true;
        }

        return false;
    }

    public boolean declineInvite(Team team, Player player) {
        return this.declineInvite(team, player.getGameProfile().id());
    }

    public boolean declineInvite(Team team, UUID id) {
        SkyMeta meta = this.metaInfo.get(id);

        if (meta == null) {
            return false;
        }

        meta.removeInvite(team.id());
        this.setDirty();
        return true;
    }

    public void renameTeam(Team team, @Nullable ServerPlayer player, String name) {
        String oldName = team.getName();
        // registry.rename internally calls team.setName(newName)
        this.registry.rename(team.id(), name);

        Component playerName = player != null ? player.getDisplayName() : Component.literal("Server");

        team.broadcast(SkyComponents.EVENT_RENAME_TEAM.apply(playerName, oldName, name), Style.EMPTY.applyFormat(ChatFormatting.DARK_RED));

        this.setDirty();
    }

    public SkyMeta getOrCreateMetaInfo(Player player) {
        return this.getOrCreateMetaInfo(player.getGameProfile().id());
    }

    public SkyMeta getOrCreateMetaInfo(UUID id) {
        return this.metaInfo.computeIfAbsent(id, meta -> new SkyMeta(this, id));
    }

    public Set<TemplatesConfig.Spawn> getPossibleSpawns(IslandPos pos, ConfiguredTemplate template) {
        if (!this.registry.containsPosition(pos)) {
            return initialPossibleSpawns(pos.getCenter(), template);
        }

        return this.registry.getByPosition(pos).getPossibleSpawns();
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
                Optional<Block> optional = configuredTemplate.getSurroundingBlocks().getRandom(random);
                optional.ifPresent(block -> level.setBlock(blockPos, block.defaultBlockState(), Block.UPDATE_CLIENTS));
            }
        });
    }

    @Override
    public void setDirty() {
        super.setDirty();
        ServerLevel level = this.getLevel();
        if (level != null) {
            SkyblockBuilder.getNetwork().updateData(level, this);
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                player.refreshTabListName();
            }
        }
    }

    public void setDirtySilently() {
        super.setDirty();
    }

    private static final class SingleWorldImpl extends SkyblockSavedData {

        private ServerLevel level;
        private Spiral spiral = new Spiral();

        public SingleWorldImpl(ServerLevel level) {
            this.level = level;
        }

        private SingleWorldImpl(@Nullable ServerLevel level, List<Team> teams, List<SkyMeta> metas, Spiral spiral) {
            this(level);
            this.spiral = spiral;
            this.loadInto(teams, metas);
        }

        @Override
        protected IslandPos nextSpawnPos(ConfiguredTemplate template) {
            if (SpawnConfig.skipCenterIslandCreation) {
                int[] pos = this.spiral.next();
                return new IslandPos(this.level, pos[0], pos[1], template);
            }

            return new IslandPos(this.level, 0, 0, template);
        }

        @Override
        protected IslandPos nextIslandPos(ConfiguredTemplate template) {
            IslandPos islandPos;
            do {
                int[] pos = this.spiral.next();
                islandPos = new IslandPos(this.level, pos[0], pos[1], template);
            } while (this.registry.containsPosition(islandPos));
            return islandPos;
        }

        @Override
        protected void onTeamCreated(Team team, ConfiguredTemplate template) {
            // Nothing extra needed in single-world mode
        }

        @Override
        protected void onTeamDeleted(Team team) {
            // Nothing extra needed in single-world mode
        }

        @Nullable
        @Override
        public ServerLevel getLevelFor(Team team) {
            return this.level;
        }

        @Override
        public void restoreInfiniverseDimensions(MinecraftServer server) {
            // Nothing extra needed in single-world mode
        }

    }

    private static final class MultiWorldImpl extends SkyblockSavedData {

        private MinecraftServer server;

        public MultiWorldImpl(MinecraftServer server) {
            this.server = server;
        }

        private MultiWorldImpl(@Nullable MinecraftServer server, List<Team> teams, List<SkyMeta> metas) {
            this(server);
            this.loadInto(teams, metas);
        }

        @Override
        protected IslandPos nextIslandPos(ConfiguredTemplate template) {
            return IslandPos.CENTERED;
        }

        @Override
        protected IslandPos nextSpawnPos(ConfiguredTemplate template) {
            return IslandPos.CENTERED;
        }

        @Override
        protected void onTeamCreated(Team team, ConfiguredTemplate template) {
            ServerLevel level = this.getOrCreateTeamDimensions(this.server, team, this.server.registryAccess());
            ResourceKey<Level> teamLevelKey = team.getTeamLevelKey();

            if (level == null) {
                throw new IllegalStateException("Failed to create dimension " + teamLevelKey.identifier());
            }

            team.setIsland(new IslandPos(level, 0, 0, template));
        }

        @Override
        protected void onTeamDeleted(Team team) {
            ResourceKey<Level> teamLevelKey = team.getTeamLevelKey();
            InfiniverseCompat.markDimensionForUnregistration(this.server, teamLevelKey);

            if (!team.isSpawn()) {
                if (WorldConfig.DimensionPerTeam.overworld && !teamLevelKey.equals(team.getTeamOverworldLevelKey())) {
                    InfiniverseCompat.markDimensionForUnregistration(this.server, team.getTeamOverworldLevelKey());
                }
                if (WorldConfig.DimensionPerTeam.nether && !teamLevelKey.equals(team.getTeamNetherLevelKey())) {
                    InfiniverseCompat.markDimensionForUnregistration(this.server, team.getTeamNetherLevelKey());
                }
            }
        }

        @Override
        public ServerLevel getLevelFor(Team team) {
            if (this.server == null) {
                return null;
            }

            if (team == null) {
                return WorldUtil.getConfiguredLevel(this.server);
            }

            return this.server.getLevel(team.getTeamLevelKey());
        }

        @Override
        public void restoreInfiniverseDimensions(MinecraftServer server) {
            RegistryAccess registryAccess = server.registryAccess();
            for (Team team : this.registry.all()) {
                this.getOrCreateTeamDimensions(server, team, registryAccess);
            }
        }

        private ServerLevel getOrCreateTeamDimensions(MinecraftServer server, Team team, RegistryAccess registryAccess) {
            ResourceKey<Level> teamLevelKey = team.getTeamLevelKey();
            ServerLevel level = InfiniverseCompat.getOrCreateLevel(server, teamLevelKey, registryAccess);

            // the dimension holding the island is always per team, the others only if they are not shared
            if (!team.isSpawn()) {
                if (WorldConfig.DimensionPerTeam.overworld && !teamLevelKey.equals(team.getTeamOverworldLevelKey())) {
                    InfiniverseCompat.getOrCreateOverworldLevel(server, team.getTeamOverworldLevelKey(), registryAccess);
                }
                if (WorldConfig.DimensionPerTeam.nether && !teamLevelKey.equals(team.getTeamNetherLevelKey())) {
                    InfiniverseCompat.getOrCreateNetherLevel(server, team.getTeamNetherLevelKey(), registryAccess);
                }
            }

            return level;
        }
    }
}
