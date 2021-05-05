package de.melanx.skyblockbuilder.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.melanx.skyblockbuilder.util.RandomUtility;
import de.melanx.skyblockbuilder.util.Spiral;
import de.melanx.skyblockbuilder.util.TemplateLoader;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/*
 * Credits go to Botania authors
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/common/world/SkyblockSavedData.java
 */
public class SkyblockSavedData extends WorldSavedData {

    private static final String NAME = "skyblock_builder";

    public static final IslandPos SPAWN_ISLAND = new IslandPos(0, 0);

    private final ServerWorld world;
    private Map<UUID, List<Team>> invites = new HashMap<>();
    private Map<String, Team> skyblocks = new HashMap<>();
    private BiMap<String, IslandPos> skyblockPositions = HashBiMap.create();
    private Spiral spiral = new Spiral();

    public SkyblockSavedData(ServerWorld world) {
        super(NAME);
        this.world = WorldUtil.getConfiguredWorld(world.getServer());
    }

    public static SkyblockSavedData get(ServerWorld world) {
        MinecraftServer server = world.getServer();
        ServerWorld configuredWorld = WorldUtil.getConfiguredWorld(server);

        DimensionSavedDataManager storage = server.func_241755_D_().getSavedData();
        return storage.getOrCreate(() -> new SkyblockSavedData(configuredWorld), NAME);
    }

    public Team getSpawn() {
        if (this.skyblocks.get("spawn") != null) {
            return this.skyblocks.get("spawn");
        }

        Team team = this.createTeam("Spawn");
        assert team != null;
        team.addPlayer(Util.DUMMY_UUID);

        this.markDirty();
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

        this.markDirty();
        return Pair.of(islandPos, team);
    }

    @Override
    public void read(CompoundNBT nbt) {
        Map<UUID, List<Team>> invites = new HashMap<>();
        Map<String, Team> skyblocks = new HashMap<>();
        BiMap<String, IslandPos> skyblockPositions = HashBiMap.create();
        for (INBT inbt : nbt.getList("Islands", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT tag = (CompoundNBT) inbt;

            IslandPos island = IslandPos.fromTag(tag.getCompound("Island"));
            Team team = new Team(this, island);
            team.deserializeNBT(tag);

            skyblocks.put(team.getName().toLowerCase(), team);
            skyblockPositions.put(team.getName().toLowerCase(), island);
        }

        for (INBT inbt : nbt.getList("Invitations", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT tag = (CompoundNBT) inbt;

            UUID player = tag.getUniqueId("Player");
            List<Team> teams = new ArrayList<>();
            for (INBT inbt1 : tag.getList("Teams", Constants.NBT.TAG_COMPOUND)) {
                CompoundNBT teamTag = (CompoundNBT) inbt1;

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
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbt) {
        ListNBT islands = new ListNBT();
        for (Team team : this.skyblocks.values()) {
            islands.add(team.serializeNBT());
        }

        ListNBT invitations = new ListNBT();
        for (Map.Entry<UUID, List<Team>> entry : this.invites.entrySet()) {
            List<Team> teams = entry.getValue();
            CompoundNBT entryTag = new CompoundNBT();
            entryTag.putUniqueId("Player", entry.getKey());

            ListNBT teamsTag = new ListNBT();
            teams.forEach(team -> {
                CompoundNBT compoundNBT = new CompoundNBT();
                compoundNBT.putString("Team", team.getName());
                teamsTag.add(compoundNBT);
            });

            entryTag.put("Teams", teamsTag);

            invitations.add(entryTag);
        }

        nbt.putIntArray("SpiralState", this.spiral.toIntArray());
        nbt.put("Islands", islands);
        nbt.put("Invitations", invitations);
        return nbt;
    }

    @Nullable
    public IslandPos getTeamIsland(String team) {
        return this.skyblockPositions.get(team.toLowerCase());
    }

    public boolean hasPlayerTeam(PlayerEntity player) {
        return this.hasPlayerTeam(player.getGameProfile().getId());
    }

    public boolean hasPlayerTeam(UUID player) {
        Team team = this.getTeamFromPlayer(player);
        return team != null && team != this.getTeam("spawn");
    }

    public boolean addPlayerToTeam(String teamName, PlayerEntity player) {
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

    public boolean addPlayerToTeam(Team team, PlayerEntity player) {
        return this.addPlayerToTeam(team, player.getGameProfile().getId());
    }

    public boolean addPlayerToTeam(Team team, UUID player) {
        team.broadcast(new TranslationTextComponent("skyblockbuilder.event.player_joined", RandomUtility.getDisplayNameByUuid(this.world, player)), Style.EMPTY.applyFormatting(TextFormatting.GOLD));
        this.getSpawn().removePlayer(player);
        team.addPlayer(player);
        this.markDirty();
        return true;
    }

    @Nullable
    public Team createTeam(String teamName) {
        if (this.teamExists(teamName)) {
            return null;
        }

        Pair<IslandPos, Team> pair = this.create(teamName);
        Team team = pair.getRight();
        List<BlockPos> possibleSpawns = new ArrayList<>(this.getPossibleSpawns(team.getIsland()));
        team.setPossibleSpawns(possibleSpawns);

        PlacementSettings settings = new PlacementSettings();
        TemplateData.get(this.world).getTemplate().func_237152_b_(this.world, team.getIsland().getCenter(), settings, new Random());

        this.skyblocks.put(team.getName().toLowerCase(), team);
        this.skyblockPositions.put(team.getName().toLowerCase(), team.getIsland());

        this.markDirty();
        return team;
    }

    @Nullable
    public Team createTeamAndJoin(String teamName, PlayerEntity player) {
        return this.createTeamAndJoin(teamName, player.getGameProfile().getId());
    }

    @Nullable
    public Team createTeamAndJoin(String teamName, UUID player) {
        Team team = this.createTeam(teamName);
        if (team == null) return null;

        team.addPlayer(player);
        this.markDirty();
        return team;
    }

    public boolean removePlayerFromTeam(PlayerEntity player) {
        return this.removePlayerFromTeam(player.getGameProfile().getId());
    }

    public boolean removePlayerFromTeam(UUID player) {
        for (Map.Entry<String, Team> entry : this.skyblocks.entrySet()) {
            Team team = entry.getValue();
            if (team.getName().equalsIgnoreCase("spawn")) continue;
            if (team.hasPlayer(player)) {
                boolean removed = team.removePlayer(player);
                if (removed) {
                    team.broadcast(new TranslationTextComponent("skyblockbuilder.event.remove_player", RandomUtility.getDisplayNameByUuid(this.world, player)), Style.EMPTY.applyFormatting(TextFormatting.RED));
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
        this.markDirty();
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
    public Team getTeamFromPlayer(PlayerEntity player) {
        return this.getTeamFromPlayer(player.getGameProfile().getId());
    }

    @Nullable
    public Team getTeamFromPlayer(UUID player) {
        Team spawn = this.getTeam("spawn");
        for (Team team : this.skyblocks.values()) {
            if (team == spawn) continue;
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

    public void addInvite(Team team, PlayerEntity invitor, PlayerEntity player) {
        this.addInvite(team, invitor, player.getGameProfile().getId());
    }

    public void addInvite(Team team, PlayerEntity invitor, UUID id) {
        List<Team> teams = this.invites.computeIfAbsent(id, uuid -> new ArrayList<>());

        if (!teams.contains(team)) {
            teams.add(team);
            PlayerEntity player = team.getWorld().getPlayerByUuid(id);
            team.broadcast(new TranslationTextComponent("skyblockbuilder.event.invite_player", invitor.getDisplayName(), RandomUtility.getDisplayNameByUuid(this.world, id)), Style.EMPTY.applyFormatting(TextFormatting.GOLD));
        }

        this.markDirty();
    }

    public boolean hasInvites(PlayerEntity player) {
        return this.hasInvites(player.getGameProfile().getId());
    }

    public boolean hasInvites(UUID player) {
        return this.invites.containsKey(player);
    }

    public boolean hasInviteFrom(Team team, PlayerEntity player) {
        return this.hasInviteFrom(team, player.getGameProfile().getId());
    }

    public boolean hasInviteFrom(Team team, UUID player) {
        return this.invites.get(player).contains(team);
    }

    public List<Team> getInvites(PlayerEntity player) {
        return this.getInvites(player.getGameProfile().getId());
    }

    public List<Team> getInvites(UUID player) {
        return this.invites.get(player);
    }

    public boolean acceptInvite(Team team, PlayerEntity player) {
        return this.acceptInvite(team, player.getGameProfile().getId());
    }

    public boolean acceptInvite(Team team, UUID id) {
        List<Team> teams = this.invites.get(id);

        if (teams == null) {
            return false;
        }

        if (teams.contains(team)) {
            team.broadcast(new TranslationTextComponent("skyblockbuilder.event.accept_invite", RandomUtility.getDisplayNameByUuid(this.world, id)), Style.EMPTY.applyFormatting(TextFormatting.GOLD));

            this.addPlayerToTeam(team.getName(), id);
            this.invites.remove(id);
            //noinspection ConstantConditions
            WorldUtil.teleportToIsland(this.world.getServer().getPlayerList().getPlayerByUUID(id), team);
            this.markDirty();

            return true;
        }

        return false;
    }

    public boolean declineInvite(Team team, PlayerEntity player) {
        return this.declineInvite(team, player.getGameProfile().getId());
    }

    public boolean declineInvite(Team team, UUID player) {
        List<Team> teams = this.invites.get(player);

        if (teams == null) {
            return false;
        }

        teams.remove(team);
        this.markDirty();
        return true;
    }

    public void renameTeam(Team team, @Nullable ServerPlayerEntity player, String name) {
        String oldName = team.getName().toLowerCase();
        this.skyblocks.remove(oldName);
        this.skyblockPositions.remove(oldName);

        team.setName(name);
        this.skyblocks.put(name.toLowerCase(), team);
        this.skyblockPositions.put(name.toLowerCase(), team.getIsland());

        ITextComponent playerName = player != null ? player.getDisplayName() : new StringTextComponent("Server");

        team.broadcast(new TranslationTextComponent("skyblockbuilder.event.rename_team", playerName, oldName, name), Style.EMPTY.applyFormatting(TextFormatting.DARK_RED));

        this.markDirty();
    }

    public Set<BlockPos> getPossibleSpawns(IslandPos pos) {
        if (!this.skyblockPositions.containsValue(pos)) {
            return initialPossibleSpawns(pos.getCenter());
        }

        return this.skyblocks.get(this.skyblockPositions.inverse().get(pos)).getPossibleSpawns();
    }

    public static Set<BlockPos> initialPossibleSpawns(BlockPos center) {
        Set<BlockPos> positions = new HashSet<>();
        for (BlockPos pos : TemplateLoader.getSpawns()) {
            positions.add(center.add(pos.toImmutable()));
        }

        return positions;
    }

    public ServerWorld getWorld() {
        return this.world;
    }
}
