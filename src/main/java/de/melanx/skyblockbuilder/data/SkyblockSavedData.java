package de.melanx.skyblockbuilder.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.template.TemplateLoader;
import de.melanx.skyblockbuilder.util.RandomUtility;
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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/*
 * Credits go to Botania authors
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/common/world/SkyblockSavedData.java
 */
public class SkyblockSavedData extends SavedData {

    private static final String NAME = "skyblock_builder";

    public static final IslandPos SPAWN_ISLAND = new IslandPos(0, 0);

    private final ServerLevel level;
    private Map<UUID, List<Team>> invites = new HashMap<>();
    private Map<String, Team> skyblocks = new HashMap<>();
    private BiMap<String, IslandPos> skyblockPositions = HashBiMap.create();
    private Spiral spiral = new Spiral();

    public SkyblockSavedData(ServerLevel level) {
        this.level = WorldUtil.getConfiguredLevel(level.getServer());
    }

    public static SkyblockSavedData get(ServerLevel level) {
        MinecraftServer server = level.getServer();
        ServerLevel configuredLevel = WorldUtil.getConfiguredLevel(server);

        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(nbt -> new SkyblockSavedData(configuredLevel).load(nbt), () -> new SkyblockSavedData(configuredLevel), NAME);
    }

    public Team getSpawn() {
        if (this.skyblocks.get("spawn") != null) {
            return this.skyblocks.get("spawn");
        }

        SkyblockBuilder.getLogger().info("Successfully generated spawn.");
        Team team = this.createTeam("Spawn");
        assert team != null;
        team.addPlayer(Util.NIL_UUID);

        this.setDirty();
        return team;
    }

    public Optional<Team> getSpawnOption() {
        return Optional.ofNullable(this.skyblocks.get("spawn"));
    }

    public Pair<IslandPos, Team> create(String teamName) {
        IslandPos islandPos;
        if (teamName.equalsIgnoreCase("spawn")) {
            islandPos = SPAWN_ISLAND;
        } else {
            do {
                int[] pos = this.spiral.next();
                islandPos = new IslandPos(pos[0], pos[1]);
            } while (this.skyblockPositions.containsValue(islandPos));
        }

        Set<BlockPos> positions = initialPossibleSpawns(islandPos.getCenter());

        Team team = new Team(this, islandPos);
        team.setPossibleSpawns(positions);
        team.setName(teamName);

        this.skyblocks.put(team.getName().toLowerCase(), team);
        this.skyblockPositions.put(team.getName().toLowerCase(), islandPos);

        this.setDirty();
        return Pair.of(islandPos, team);
    }

    public SkyblockSavedData load(CompoundTag nbt) {
        Map<UUID, List<Team>> invites = new HashMap<>();
        Map<String, Team> skyblocks = new HashMap<>();
        BiMap<String, IslandPos> skyblockPositions = HashBiMap.create();
        for (Tag inbt : nbt.getList("Islands", Constants.NBT.TAG_COMPOUND)) {
            CompoundTag tag = (CompoundTag) inbt;

            IslandPos island = IslandPos.fromTag(tag.getCompound("Island"));
            Team team = new Team(this, island);
            team.deserializeNBT(tag);

            skyblocks.put(team.getName().toLowerCase(), team);
            skyblockPositions.put(team.getName().toLowerCase(), island);
        }

        for (Tag inbt : nbt.getList("Invitations", Constants.NBT.TAG_COMPOUND)) {
            CompoundTag tag = (CompoundTag) inbt;

            UUID player = tag.getUUID("Player");
            List<Team> teams = new ArrayList<>();
            for (Tag inbt1 : tag.getList("Teams", Constants.NBT.TAG_COMPOUND)) {
                CompoundTag teamTag = (CompoundTag) inbt1;

                String teamName = teamTag.getString("Team");
                Team team = skyblocks.get(teamName.toLowerCase());
                if (team != null) {
                    teams.add(team);
                }
            }
            if (!teams.isEmpty()) {
                invites.put(player, teams);
            }
        }
        this.invites = invites;
        this.skyblocks = skyblocks;
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

        ListTag invitations = new ListTag();
        for (Map.Entry<UUID, List<Team>> entry : this.invites.entrySet()) {
            List<Team> teams = entry.getValue();
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("Player", entry.getKey());

            ListTag teamsTag = new ListTag();
            teams.forEach(team -> {
                CompoundTag compoundNBT = new CompoundTag();
                compoundNBT.putString("Team", team.getName());
                teamsTag.add(compoundNBT);
            });

            entryTag.put("Teams", teamsTag);

            invitations.add(entryTag);
        }

        compound.putIntArray("SpiralState", this.spiral.toIntArray());
        compound.put("Islands", islands);
        compound.put("Invitations", invitations);
        return compound;
    }

    @Nullable
    public IslandPos getTeamIsland(String team) {
        return this.skyblockPositions.get(team.toLowerCase());
    }

    public boolean hasPlayerTeam(Player player) {
        return this.hasPlayerTeam(player.getGameProfile().getId());
    }

    public boolean hasPlayerTeam(UUID player) {
        Team team = this.getTeamFromPlayer(player);
        return team != null && !team.isSpawn();
    }

    public boolean addPlayerToTeam(String teamName, Player player) {
        return this.addPlayerToTeam(teamName, player.getGameProfile().getId());
    }

    public boolean addPlayerToTeam(String teamName, UUID player) {
        for (Team team : this.skyblocks.values()) {
            if (team.getName().equals(teamName)) {
                return this.addPlayerToTeam(team, player);
            }
        }

        return false;
    }

    public boolean addPlayerToTeam(Team team, Player player) {
        return this.addPlayerToTeam(team, player.getGameProfile().getId());
    }

    public boolean addPlayerToTeam(Team team, UUID player) {
        if (team.getIsland() != SPAWN_ISLAND) {
            team.broadcast(new TranslatableComponent("skyblockbuilder.event.player_joined", RandomUtility.getDisplayNameByUuid(this.level, player)), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        }
        this.getSpawn().removePlayer(player);
        team.addPlayer(player);
        this.setDirty();
        return true;
    }

    @Nullable
    public Team createTeam(String teamName) {
        return this.createTeam(teamName, TemplateData.get(this.level).getTemplate());
    }

    @Nullable
    public Team createTeam(String teamName, StructureTemplate template) {
        if (this.teamExists(teamName)) {
            return null;
        }

        Pair<IslandPos, Team> pair = this.create(teamName);
        Team team = pair.getRight();
        List<BlockPos> possibleSpawns = new ArrayList<>(this.getPossibleSpawns(team.getIsland()));
        team.setPossibleSpawns(possibleSpawns);

        StructurePlaceSettings settings = new StructurePlaceSettings();
        BlockPos center = team.getIsland().getCenter();
        template.placeInWorld(this.level, center, center, settings, new Random(), 2);

        this.skyblocks.put(team.getName().toLowerCase(), team);
        this.skyblockPositions.put(team.getName().toLowerCase(), team.getIsland());

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
        for (Map.Entry<String, Team> entry : this.skyblocks.entrySet()) {
            Team team = entry.getValue();
            if (team.isSpawn()) continue;
            if (team.hasPlayer(player)) {
                boolean removed = team.removePlayer(player);
                if (removed) {
                    team.broadcast(new TranslatableComponent("skyblockbuilder.event.remove_player", RandomUtility.getDisplayNameByUuid(this.level, player)), Style.EMPTY.applyFormat(ChatFormatting.RED));
                    //noinspection ConstantConditions
                    this.getTeam("spawn").addPlayer(player);
                }
                return removed;
            }
        }
        return false;
    }

    public void removeAllPlayersFromTeam(Team team) {
        Set<UUID> players = new HashSet<>(team.getPlayers());
        team.removeAllPlayers();
        Team spawn = this.getSpawn();
        for (UUID player : players) {
            this.addPlayerToTeam(spawn, player);
        }
        this.setDirty();
    }

    @Nullable
    public Team getTeam(String name) {
        return this.skyblocks.get(name.toLowerCase());
    }

    public boolean deleteTeam(String name) {
        Iterator<Map.Entry<String, Team>> itr = this.skyblocks.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Team> entry = itr.next();
            Team team = entry.getValue();
            if (team.getName().equalsIgnoreCase(name)) {
                this.skyblockPositions.inverse().remove(team.getIsland());
                itr.remove();
                return true;
            }
        }
        return false;
    }

    public boolean deleteTeam(Team team) {
        Team removedTeam = this.skyblocks.remove(team.getName());

        //noinspection ConstantConditions
        return removedTeam != null && this.getTeam("spawn").addPlayers(removedTeam.getPlayers());
    }

    @Nullable
    public Team getTeamFromPlayer(Player player) {
        return this.getTeamFromPlayer(player.getGameProfile().getId());
    }

    @Nullable
    public Team getTeamFromPlayer(UUID player) {
        for (Team team : this.skyblocks.values()) {
            if (team.isSpawn()) continue;
            if (team.getPlayers().contains(player)) {
                return team;
            }
        }

        return null;
    }

    public boolean teamExists(String name) {
        return this.skyblocks.containsKey(name.toLowerCase());
    }

    public Collection<Team> getTeams() {
        return this.skyblocks.values();
    }

    public void addInvite(Team team, Player invitor, Player player) {
        this.addInvite(team, invitor, player.getGameProfile().getId());
    }

    public void addInvite(Team team, Player invitor, UUID id) {
        List<Team> teams = this.invites.computeIfAbsent(id, uuid -> new ArrayList<>());

        if (!teams.contains(team)) {
            teams.add(team);
            Player player = team.getLevel().getPlayerByUUID(id);
            team.broadcast(new TranslatableComponent("skyblockbuilder.event.invite_player", invitor.getDisplayName(), RandomUtility.getDisplayNameByUuid(this.level, id)), Style.EMPTY.applyFormat(ChatFormatting.GOLD));
        }

        this.setDirty();
    }

    public boolean hasInvites(Player player) {
        return this.hasInvites(player.getGameProfile().getId());
    }

    public boolean hasInvites(UUID player) {
        return this.invites.containsKey(player);
    }

    public boolean hasInviteFrom(Team team, Player player) {
        return this.hasInviteFrom(team, player.getGameProfile().getId());
    }

    public boolean hasInviteFrom(Team team, UUID player) {
        return this.invites.get(player).contains(team);
    }

    public List<Team> getInvites(Player player) {
        return this.getInvites(player.getGameProfile().getId());
    }

    public List<Team> getInvites(UUID player) {
        return this.invites.get(player);
    }

    public boolean acceptInvite(Team team, Player player) {
        return this.acceptInvite(team, player.getGameProfile().getId());
    }

    public boolean acceptInvite(Team team, UUID id) {
        List<Team> teams = this.invites.get(id);

        if (teams == null) {
            return false;
        }

        if (teams.contains(team)) {
            team.broadcast(new TranslatableComponent("skyblockbuilder.event.accept_invite", RandomUtility.getDisplayNameByUuid(this.level, id)), Style.EMPTY.applyFormat(ChatFormatting.GOLD));

            this.addPlayerToTeam(team.getName(), id);
            this.invites.remove(id);
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

    public boolean declineInvite(Team team, UUID player) {
        List<Team> teams = this.invites.get(player);

        if (teams == null) {
            return false;
        }

        teams.remove(team);
        this.setDirty();
        return true;
    }

    public void renameTeam(Team team, @Nullable ServerPlayer player, String name) {
        String oldName = team.getName().toLowerCase();
        this.skyblocks.remove(oldName);
        this.skyblockPositions.remove(oldName);

        team.setName(name);
        this.skyblocks.put(name.toLowerCase(), team);
        this.skyblockPositions.put(name.toLowerCase(), team.getIsland());

        Component playerName = player != null ? player.getDisplayName() : new TextComponent("Server");

        team.broadcast(new TranslatableComponent("skyblockbuilder.event.rename_team", playerName, oldName, name), Style.EMPTY.applyFormat(ChatFormatting.DARK_RED));

        this.setDirty();
    }

    public Set<BlockPos> getPossibleSpawns(IslandPos pos) {
        if (!this.skyblockPositions.containsValue(pos)) {
            return initialPossibleSpawns(pos.getCenter());
        }

        return this.skyblocks.get(this.skyblockPositions.inverse().get(pos)).getPossibleSpawns();
    }

    public static Set<BlockPos> initialPossibleSpawns(BlockPos center) {
        Set<BlockPos> positions = new HashSet<>();
        for (BlockPos pos : TemplateLoader.getCurrentSpawns()) {
            positions.add(center.offset(pos.immutable()));
        }

        return positions;
    }

    public ServerLevel getLevel() {
        return this.level;
    }
}
