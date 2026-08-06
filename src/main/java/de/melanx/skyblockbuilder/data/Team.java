package de.melanx.skyblockbuilder.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.config.common.SpawnConfig;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.util.SkyComponents;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.moddingx.libx.annotation.api.Codecs;
import org.moddingx.libx.annotation.codec.PrimaryConstructor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Team {

    private static final String TEAM_ID = "team_id";
    private static final String ISLAND = "island";
    private static final String NAME = "name";
    private static final String VISITS = "visits";
    private static final String ALLOW_JOIN_REQUESTS = "allow_join_requests";
    private static final String NETHER_SPREADS_PLACED = "nether_spreads_placed";
    private static final String CREATED_AT = "created_at";
    private static final String LAST_CHANGED = "last_changed";
    private static final String PLAYERS = "players";
    private static final String SPAWNS = "spawns";
    private static final String DEFAULT_SPAWNS = "default_spawns";
    private static final String JOIN_REQUESTS = "join_requests";
    private static final String PLACED_SPREADS = "placed_spreads";

    public static final int MAX_NAME_LENGTH = 64;

    // Spawn level if using Infiniverse
    public static final ResourceKey<Level> SPAWN_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION, SkyblockBuilder.getInstance().id("spawn"));

    public static final Codec<Team> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf(TEAM_ID).forGetter(team -> team.teamId),
            IslandPos.CODEC.fieldOf(ISLAND).forGetter(team -> team.island),
            Codec.STRING.optionalFieldOf(NAME, "").forGetter(team -> team.name),
            Codec.BOOL.optionalFieldOf(VISITS, false).forGetter(team -> team.allowVisits),
            Codec.BOOL.optionalFieldOf(ALLOW_JOIN_REQUESTS, false).forGetter(team -> team.allowJoinRequests),
            Codec.BOOL.optionalFieldOf(NETHER_SPREADS_PLACED, false).forGetter(team -> team.netherSpreadsPlaced),
            Codec.LONG.optionalFieldOf(CREATED_AT, 0L).forGetter(team -> team.createdAt),
            Codec.LONG.optionalFieldOf(LAST_CHANGED, 0L).forGetter(team -> team.lastChanged),
            UUIDUtil.CODEC.listOf().optionalFieldOf(PLAYERS, List.of()).forGetter(team -> List.copyOf(team.players)),
            TemplatesConfig.Spawn.CODEC.listOf().optionalFieldOf(SPAWNS, List.of()).forGetter(team -> List.copyOf(team.possibleSpawns)),
            TemplatesConfig.Spawn.CODEC.listOf().optionalFieldOf(DEFAULT_SPAWNS).forGetter(team -> Optional.of(List.copyOf(team.defaultPossibleSpawns))),
            UUIDUtil.CODEC.listOf().optionalFieldOf(JOIN_REQUESTS, List.of()).forGetter(team -> List.copyOf(team.joinRequests)),
            PlacedSpread.CODEC.listOf().optionalFieldOf(PLACED_SPREADS, List.of()).forGetter(team -> team.placedSpreads.values().stream().flatMap(Set::stream).toList())
    ).apply(instance, Team::new));

    private final Set<UUID> players = new CopyOnWriteArraySet<>();
    private final Set<UUID> joinRequests = new CopyOnWriteArraySet<>();
    private final Set<TemplatesConfig.Spawn> possibleSpawns = new CopyOnWriteArraySet<>();
    private final Set<TemplatesConfig.Spawn> defaultPossibleSpawns = new CopyOnWriteArraySet<>();
    private final Map<String, Set<PlacedSpread>> placedSpreads = new ConcurrentHashMap<>();
    private final UUID teamId;
    private final long createdAt;

    private SkyblockSavedData data;
    private ResourceKey<Level> teamLevelKey;
    private IslandPos island;
    private String name;
    private boolean allowVisits;
    private boolean allowJoinRequests;
    private boolean netherSpreadsPlaced;
    private long lastChanged;

    private Team(UUID teamId, IslandPos island, String name, boolean allowVisits, boolean allowJoinRequests,
            boolean netherSpreadsPlaced, long createdAt, long lastChanged, List<UUID> players,
            List<TemplatesConfig.Spawn> possibleSpawns, Optional<List<TemplatesConfig.Spawn>> defaultPossibleSpawns,
            List<UUID> joinRequests, List<PlacedSpread> placedSpreads) {
        this.data = null;
        this.teamId = teamId;
        this.island = island;
        this.name = name;
        this.allowVisits = allowVisits;
        this.allowJoinRequests = allowJoinRequests;
        this.netherSpreadsPlaced = netherSpreadsPlaced;
        this.createdAt = createdAt;
        this.lastChanged = lastChanged;
        this.players.addAll(players);
        this.possibleSpawns.addAll(possibleSpawns);
        this.defaultPossibleSpawns.addAll(defaultPossibleSpawns.orElse(possibleSpawns));
        this.joinRequests.addAll(joinRequests);
        placedSpreads.forEach(spread -> this.placedSpreads
                .computeIfAbsent(spread.name(), key -> ConcurrentHashMap.newKeySet())
                .add(spread));
    }

    public Team(SkyblockSavedData data, IslandPos island) {
        this(data, island, UUID.randomUUID());
    }

    public Team(SkyblockSavedData data, IslandPos island, UUID teamId) {
        this.data = data;
        this.island = island;
        this.teamId = teamId;
        this.allowVisits = false;
        this.createdAt = System.currentTimeMillis();
        this.lastChanged = System.currentTimeMillis();
    }

    void bindData(SkyblockSavedData data) {
        this.data = data;
    }

    public boolean isSpawn() {
        return Objects.equals(this.teamId, SkyblockSavedData.SPAWN_ID);
    }

    public String getName() {
        return this.name;
    }

    public UUID id() {
        return this.teamId;
    }

    public ResourceKey<Level> getTeamLevelKey() {
        // need to do this in case id is null first
        if (this.teamLevelKey == null) {
            this.teamLevelKey = this.resolveTeamLevelKey();
        }

        return this.teamLevelKey;
    }

    // The island lives in the spawn dimension, so the team dimension holding it is named after it. This must only
    // depend on the configured id, never on whether that dimension exists, otherwise the team would be moved to
    // another dimension as soon as a missing spawn dimension gets added.
    private ResourceKey<Level> resolveTeamLevelKey() {
        if (this.isSpawn()) {
            return Team.SPAWN_LEVEL_KEY;
        }

        if (SpawnConfig.spawnDimension == Level.OVERWORLD) {
            return this.getTeamOverworldLevelKey();
        }

        if (SpawnConfig.spawnDimension == Level.NETHER) {
            return this.getTeamNetherLevelKey();
        }

        return this.getTeamMainLevelKey();
    }

    public ResourceKey<Level> getTeamMainLevelKey() {
        return this.getTeamLevelKey("main");
    }

    public ResourceKey<Level> getTeamOverworldLevelKey() {
        return this.getTeamLevelKey("overworld");
    }

    public ResourceKey<Level> getTeamNetherLevelKey() {
        return this.getTeamLevelKey("nether");
    }

    private ResourceKey<Level> getTeamLevelKey(String suffix) {
        return ResourceKey.create(Registries.DIMENSION, SkyblockBuilder.getInstance().id(
                this.teamId.toString().replace("-", "") + "_" + suffix));
    }

    public void setName(String name) {
        this.name = name;
        this.updateLastChanged();
    }

    public IslandPos getIsland() {
        return this.island;
    }

    public void setIsland(IslandPos island) {
        this.island = island;
        this.updateLastChanged();
    }

    public Set<UUID> getPlayers() {
        return this.players;
    }

    public void setPlayers(Collection<UUID> players) {
        this.players.clear();
        //noinspection ConstantConditions
        PlayerList playerList = this.getLevel().getServer().getPlayerList();
        if (ModList.get().isLoaded("minemention")) {
            for (UUID id : players) {
                MineMentionCompat.updateMentions(playerList.getPlayer(id));
            }
        }
        this.players.addAll(players);
        this.updateLastChanged();
    }

    public Set<TemplatesConfig.Spawn> getPossibleSpawns() {
        return Set.copyOf(this.possibleSpawns);
    }

    public Set<TemplatesConfig.Spawn> getDefaultPossibleSpawns() {
        return Set.copyOf(this.defaultPossibleSpawns);
    }

    public void setPossibleSpawns(Collection<TemplatesConfig.Spawn> spawns) {
        this.possibleSpawns.clear();
        this.defaultPossibleSpawns.clear();
        this.possibleSpawns.addAll(spawns);
        this.defaultPossibleSpawns.addAll(spawns);
        this.updateLastChanged();
    }

    public void addPossibleSpawn(TemplatesConfig.Spawn spawn) {
        this.possibleSpawns.add(spawn);
        this.updateLastChanged();
    }

    public void addPossibleSpawn(BlockPos pos, WorldUtil.SpawnDirection direction) {
        this.addPossibleSpawn(new TemplatesConfig.Spawn(pos, direction));
    }

    public boolean removePossibleSpawn(BlockPos pos) {
        if (this.possibleSpawns.size() <= 1) {
            return false;
        }

        for (TemplatesConfig.Spawn possibleSpawn : this.possibleSpawns) {
            if (possibleSpawn.pos().equals(pos)) {
                boolean remove = this.possibleSpawns.remove(possibleSpawn);
                this.updateLastChanged();

                return remove;
            }
        }

        return false;
    }

    public boolean allowsVisits() {
        return this.allowVisits;
    }

    public boolean toggleAllowVisits() {
        this.allowVisits = !this.allowVisits;
        this.updateLastChanged();
        return this.allowVisits;
    }

    public void setAllowVisit(boolean enabled) {
        if (this.allowVisits != enabled) {
            this.allowVisits = enabled;
            this.updateLastChanged();
        }
    }

    public boolean addPlayer(UUID player) {
        boolean added = this.players.add(player);
        if (added) {
            if (ModList.get().isLoaded("minemention")) {
                //noinspection ConstantConditions
                MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(player));
            }
            if (!this.isSpawn()) {
                this.data.getSpawn().removePlayer(player);
            }
            this.data.getOrCreateMetaInfo(player).setTeamId(this.teamId);
            this.updateLastChanged();
        }
        return added;
    }

    public boolean addPlayer(Player player) {
        return this.addPlayer(player.getGameProfile().id());
    }

    public boolean addPlayers(Collection<UUID> players) {
        boolean added = this.players.addAll(players);
        if (added) {
            if (ModList.get().isLoaded("minemention")) {
                for (UUID id : players) {
                    //noinspection ConstantConditions
                    MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(id));
                    this.data.getOrCreateMetaInfo(id).setTeamId(this.teamId);
                }
            }
            this.updateLastChanged();
        }

        return added;
    }

    public boolean removePlayer(Player player) {
        return this.removePlayer(player.getGameProfile().id());
    }

    public boolean removePlayer(UUID player) {
        boolean removed = this.players.remove(player);
        if (ModList.get().isLoaded("minemention")) {
            //noinspection ConstantConditions
            MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(player));
        }
        if (removed) {
            this.data.getOrCreateMetaInfo(player).addPreviousTeamId(this.teamId);
            this.lastChanged = System.currentTimeMillis();
        }
        this.data.setDirty();

        return removed;
    }

    public void removePlayers(Collection<UUID> players) {
        for (UUID id : players) {
            boolean removed = this.players.remove(id);
            if (ModList.get().isLoaded("minemention")) {
                //noinspection ConstantConditions
                MineMentionCompat.updateMentions(this.getLevel().getServer().getPlayerList().getPlayer(id));
            }
            if (removed) {
                this.data.getOrCreateMetaInfo(id).addPreviousTeamId(this.teamId);
            }
        }
        this.updateLastChanged();
    }

    public void removeAllPlayers() {
        this.removePlayers(this.players);
    }

    public boolean hasPlayer(UUID player) {
        return this.players.contains(player);
    }

    public boolean hasPlayer(Player player) {
        return this.hasPlayer(player.getGameProfile().id());
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public boolean allowsJoinRequests() {
        return this.allowJoinRequests;
    }

    public boolean toggleAllowJoinRequest() {
        this.allowJoinRequests = !this.allowJoinRequests;
        this.updateLastChanged();
        return this.allowJoinRequests;
    }

    public void setAllowJoinRequest(boolean enabled) {
        if (this.allowJoinRequests != enabled) {
            this.allowJoinRequests = enabled;
            this.updateLastChanged();
        }
    }

    public Set<UUID> getJoinRequests() {
        return this.joinRequests;
    }

    public void addJoinRequest(Player player) {
        this.addJoinRequest(player.getGameProfile().id());
    }

    public void addJoinRequest(UUID id) {
        this.joinRequests.add(id);
        this.data.setDirty();
    }

    public void removeJoinRequest(Player player) {
        this.removeJoinRequest(player.getGameProfile().id());
    }

    public void removeJoinRequest(UUID id) {
        this.joinRequests.remove(id);
        this.data.setDirty();
    }

    public void resetJoinRequests() {
        this.joinRequests.clear();
        this.data.setDirty();
    }

    public void addSpread(String spreadName, BlockPos pos, BlockPos size) {
        this.addSpread(new PlacedSpread(spreadName, pos, size));
    }

    public void addSpread(PlacedSpread placedSpread) {
        this.placedSpreads.computeIfAbsent(placedSpread.name(), s -> new HashSet<>()).add(placedSpread);
        this.data.setDirty();
    }

    public Map<String, Set<PlacedSpread>> getPlacedSpreads() {
        return this.placedSpreads;
    }

    public Set<PlacedSpread> getPlacedSpreads(String spreadName) {
        return this.placedSpreads.containsKey(spreadName) ? this.placedSpreads.get(spreadName) : Set.of();
    }

    public Set<String> getAllSpreadNames() {
        return this.placedSpreads.keySet();
    }

    public void markNetherSpreadsPlaced() {
        this.netherSpreadsPlaced = true;
        this.data.setDirty();
    }

    public boolean isNetherSpreadsPlaced() {
        return this.netherSpreadsPlaced;
    }

    public void sendJoinRequest(Player requestingPlayer) {
        this.addJoinRequest(requestingPlayer.getGameProfile().id());
        MutableComponent component = SkyComponents.EVENT_JOIN_REQUEST0.apply(requestingPlayer.getDisplayName());
        component.append(Component.literal("/skyblock team accept " + requestingPlayer.getDisplayName().getString()).setStyle(Style.EMPTY
                .withHoverEvent(InviteCommand.COPY_TEXT)
                .withClickEvent(new ClickEvent.SuggestCommand("/skyblock team accept " + requestingPlayer.getDisplayName().getString()))
                .applyFormats(ChatFormatting.UNDERLINE, ChatFormatting.GOLD)));
        component.append(SkyComponents.EVENT_JOIN_REQUEST1);
        this.broadcast(component, Style.EMPTY.applyFormat(ChatFormatting.GOLD));
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public long getLastChanged() {
        return this.lastChanged;
    }

    public void updateLastChanged() {
        this.lastChanged = System.currentTimeMillis();
        this.data.setDirty();
    }

    @Nullable
    public ServerLevel getLevel() {
        return this.data.getLevelFor(this);
    }

    public void broadcast(MutableComponent msg, Style style) {
        if (this.getLevel() == null || this.getLevel().isClientSide()) {
            return;
        }

        PlayerList playerList = this.getLevel().getServer().getPlayerList();
        this.players.forEach(uuid -> {
            ServerPlayer player = playerList.getPlayer(uuid);
            if (player != null) {
                MutableComponent component = Component.literal("[" + this.name + "] ").setStyle(Style.EMPTY);
                player.sendSystemMessage(component.append(msg.withStyle(style)));
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Team)) {
            return false;
        }

        Team team = (Team) o;
        return this.name.equals(team.name) && this.island.equals(team.island);
    }

    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result * this.island.hashCode();
        return result;
    }

    @PrimaryConstructor
    public record PlacedSpread(String name, BlockPos pos, BlockPos size) {

        public static final Codec<PlacedSpread> CODEC = Codecs.get(SkyblockBuilder.class, PlacedSpread.class);
    }
}
