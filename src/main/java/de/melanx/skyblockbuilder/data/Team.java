package de.melanx.skyblockbuilder.data;

import de.melanx.skyblockbuilder.commands.invitation.InviteCommand;
import de.melanx.skyblockbuilder.compat.minemention.MineMentionCompat;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class Team {

    private final SkyblockSavedData data;
    private final Set<UUID> players;
    private final Set<UUID> joinRequests;
    private final Set<TemplatesConfig.Spawn> possibleSpawns;
    private final Set<TemplatesConfig.Spawn> defaultPossibleSpawns;
    private final Map<String, Set<PlacedSpread>> placedSpreads;
    private UUID teamId;
    private IslandPos island;
    private String name;
    private boolean allowVisits;
    private boolean allowJoinRequests;
    private long createdAt;
    private long lastChanged;

    private Team(SkyblockSavedData data) {
        this(data, null, null);
    }

    public Team(SkyblockSavedData data, IslandPos island) {
        this(data, island, UUID.randomUUID());
    }

    public Team(SkyblockSavedData data, IslandPos island, UUID teamId) {
        this.data = data;
        this.island = island;
        this.players = new HashSet<>();
        this.possibleSpawns = new HashSet<>();
        this.defaultPossibleSpawns = new HashSet<>();
        this.placedSpreads = new HashMap<>();
        this.joinRequests = new HashSet<>();
        this.teamId = teamId;
        this.allowVisits = false;
        this.createdAt = System.currentTimeMillis();
        this.lastChanged = System.currentTimeMillis();
    }

    public static Team create(SkyblockSavedData data, CompoundTag tag) {
        Team team = new Team(data);
        team.deserializeNBT(tag);

        return team;
    }

    public boolean isSpawn() {
        return Objects.equals(this.teamId, SkyblockSavedData.SPAWN_ID);
    }

    public String getName() {
        return this.name;
    }

    public UUID getId() {
        return this.teamId;
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

    public void addPossibleSpawn(BlockPos pos, WorldUtil.Directions direction) {
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
        return this.addPlayer(player.getGameProfile().getId());
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
        return this.removePlayer(player.getGameProfile().getId());
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
        return this.hasPlayer(player.getGameProfile().getId());
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
        this.addJoinRequest(player.getGameProfile().getId());
    }

    public void addJoinRequest(UUID id) {
        this.joinRequests.add(id);
        this.data.setDirty();
    }

    public void removeJoinRequest(Player player) {
        this.removeJoinRequest(player.getGameProfile().getId());
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

    public void sendJoinRequest(Player requestingPlayer) {
        this.addJoinRequest(requestingPlayer.getGameProfile().getId());
        MutableComponent component = Component.translatable("skyblockbuilder.event.join_request0", requestingPlayer.getDisplayName());
        component.append(Component.literal("/skyblock team accept " + requestingPlayer.getDisplayName().getString()).setStyle(Style.EMPTY
                .withHoverEvent(InviteCommand.COPY_TEXT)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skyblock team accept " + requestingPlayer.getDisplayName().getString()))
                .applyFormats(ChatFormatting.UNDERLINE, ChatFormatting.GOLD)));
        component.append(Component.translatable("skyblockbuilder.event.join_request1"));
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
        return this.data.getLevel();
    }

    public void broadcast(MutableComponent msg, Style style) {
        if (this.getLevel() == null || this.getLevel().isClientSide) {
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

    @Nonnull
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        nbt.putUUID("TeamId", this.teamId);
        nbt.put("Island", this.island.toTag());
        nbt.putString("Name", this.name != null ? this.name : "");
        nbt.putBoolean("Visits", this.allowVisits);
        nbt.putBoolean("AllowJoinRequests", this.allowJoinRequests);
        nbt.putLong("CreatedAt", this.createdAt);
        nbt.putLong("LastChanged", this.lastChanged);

        ListTag players = new ListTag();
        for (UUID player : this.players) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", player);

            players.add(playerTag);
        }

        ListTag spawns = new ListTag();
        for (TemplatesConfig.Spawn spawn : this.possibleSpawns) {
            CompoundTag posTag = WorldUtil.getPosTag(spawn.pos());
            posTag.putString("Direction", spawn.direction().name());

            spawns.add(posTag);
        }

        ListTag defaultSpawns = new ListTag();
        for (TemplatesConfig.Spawn spawn : this.defaultPossibleSpawns) {
            CompoundTag posTag = WorldUtil.getPosTag(spawn.pos());
            posTag.putString("Direction", spawn.direction().name());

            defaultSpawns.add(posTag);
        }

        ListTag joinRequests = new ListTag();
        for (UUID id : this.joinRequests) {
            CompoundTag idTag = new CompoundTag();
            idTag.putUUID("Id", id);

            joinRequests.add(idTag);
        }

        CompoundTag placedSpreads = new CompoundTag();
        for (Map.Entry<String, Set<PlacedSpread>> entry : this.placedSpreads.entrySet()) {
            ListTag namedSpreads = new ListTag();
            for (PlacedSpread placedSpread : entry.getValue()) {
                CompoundTag tag = new CompoundTag();
                tag.putString("Name", placedSpread.name());
                tag.put("Pos", WorldUtil.getPosTag(placedSpread.pos()));
                tag.put("Size", WorldUtil.getPosTag(placedSpread.size()));
                namedSpreads.add(tag);
            }
            placedSpreads.put(entry.getKey(), namedSpreads);
        }
        nbt.put("PlacedSpreads", placedSpreads);

        nbt.put("Players", players);
        nbt.put("Spawns", spawns);
        nbt.put("DefaultSpawns", defaultSpawns);
        nbt.put("JoinRequests", joinRequests);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.teamId = nbt.getUUID("TeamId");
        this.island = IslandPos.fromTag(nbt.getCompound("Island"));
        this.name = nbt.getString("Name");
        this.allowVisits = nbt.getBoolean("Visits");
        this.allowJoinRequests = nbt.getBoolean("AllowJoinRequests");
        this.createdAt = nbt.getLong("CreatedAt");
        this.lastChanged = nbt.getLong("LastChanged");

        ListTag players = nbt.getList("Players", Tag.TAG_COMPOUND);
        this.players.clear();
        for (Tag player : players) {
            this.players.add(((CompoundTag) player).getUUID("Player"));
        }

        ListTag spawns = nbt.getList("Spawns", Tag.TAG_COMPOUND);
        this.possibleSpawns.clear();
        for (Tag tag : spawns) {
            CompoundTag posTag = (CompoundTag) tag;
            BlockPos pos = WorldUtil.getPosFromTag(posTag);
            WorldUtil.Directions direction = WorldUtil.Directions.valueOf(posTag.getString("Direction"));
            this.possibleSpawns.add(new TemplatesConfig.Spawn(pos, direction));
        }

        ListTag defaultSpawns = nbt.getList("DefaultSpawns", Tag.TAG_COMPOUND);
        this.defaultPossibleSpawns.clear();
        for (Tag tag : defaultSpawns) {
            CompoundTag posTag = (CompoundTag) tag;
            BlockPos pos = WorldUtil.getPosFromTag(posTag);
            WorldUtil.Directions direction = WorldUtil.Directions.valueOf(posTag.getString("Direction"));
            this.defaultPossibleSpawns.add(new TemplatesConfig.Spawn(pos, direction));
        }

        ListTag joinRequests = nbt.getList("JoinRequests", Tag.TAG_COMPOUND);
        this.joinRequests.clear();
        for (Tag id : joinRequests) {
            this.joinRequests.add(((CompoundTag) id).getUUID("Id"));
        }

        CompoundTag placedSpreads = nbt.getCompound("PlacedSpreads");
        this.placedSpreads.clear();
        for (String key : placedSpreads.getAllKeys()) {
            ListTag list = placedSpreads.getList(key, Tag.TAG_COMPOUND);
            Set<PlacedSpread> namedSpreads = new HashSet<>();
            for (Tag tag : list) {
                CompoundTag ctag = ((CompoundTag) tag);
                String name = ctag.getString("Name");
                BlockPos pos = WorldUtil.getPosFromTag(ctag.getCompound("Pos"));
                BlockPos size = WorldUtil.getPosFromTag(ctag.getCompound("Size"));

                PlacedSpread placedSpread = new PlacedSpread(name, pos, size);
                namedSpreads.add(placedSpread);
            }
            this.placedSpreads.put(key, namedSpreads);
        }
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

    public record PlacedSpread(String name, BlockPos pos, BlockPos size) {}
}
