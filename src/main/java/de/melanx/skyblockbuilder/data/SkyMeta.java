package de.melanx.skyblockbuilder.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SkyMeta {

    private static final String OWNER_ID = "owner_id";
    private static final String TEAM_ID = "team_id";
    private static final String PREVIOUS_TEAM_IDS = "previous_team_ids";
    private static final String INVITATIONS = "invitations";
    private static final String LAST_HOME_TELEPORT = "last_home_teleport";
    private static final String LAST_SPAWN_TELEPORT = "last_spawn_teleport";
    private static final String LAST_VISIT_TELEPORT = "last_visit_teleport";

    private final Set<UUID> previousTeamIds = Sets.newHashSet();
    private final List<UUID> invites = Lists.newArrayList();
    private final SkyblockSavedData data;
    private UUID owner;
    private UUID teamId = SkyblockSavedData.SPAWN_ID;
    private long lastHomeTeleport;
    private long lastSpawnTeleport;
    private long lastVisitTeleport;

    public static SkyMeta get(SkyblockSavedData data, @Nonnull CompoundTag nbt) {
        return new SkyMeta(data, null).load(nbt);
    }

    public SkyMeta(SkyblockSavedData data, UUID owner) {
        this.data = data;
        this.owner = owner;
    }

    @Nonnull
    public UUID getOwner() {
        return this.owner;
    }

    @Nonnull
    public UUID getTeamId() {
        return this.teamId;
    }

    public void setTeamId(@Nonnull UUID id) {
        this.teamId = id;
        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public Set<UUID> getPreviousTeamIds() {
        return this.previousTeamIds;
    }

    public void addPreviousTeamId(@Nonnull UUID id) {
        this.previousTeamIds.add(id);
        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public List<UUID> getInvites() {
        return this.invites;
    }

    public void addInvite(@Nonnull UUID teamId) {
        this.invites.add(teamId);
        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public void removeInvite(@Nonnull UUID teamId) {
        this.invites.remove(teamId);
        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public void resetInvites() {
        this.invites.clear();
        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public long getLastTeleport(TeleportType type) {
        return switch (type) {
            case SPAWN -> this.lastSpawnTeleport;
            case HOME -> this.lastHomeTeleport;
            case VISIT -> this.lastVisitTeleport;
        };
    }

    public void setLastTeleport(TeleportType type, long gameTime) {
        switch (type) {
            case SPAWN -> this.lastSpawnTeleport = gameTime;
            case HOME -> this.lastHomeTeleport = gameTime;
            case VISIT -> this.lastVisitTeleport = gameTime;
        }

        if (this.data != null) {
            this.data.setDirtySilently();
        }
    }

    public boolean canTeleport(TeleportType type, long gameTime) {
        long lastTeleport = this.getLastTeleport(type);
        int cooldown = switch (type) {
            case SPAWN -> PermissionsConfig.Teleports.Cooldowns.spawnCooldown;
            case HOME -> PermissionsConfig.Teleports.Cooldowns.homeCooldown;
            case VISIT -> PermissionsConfig.Teleports.Cooldowns.visitCooldown;
        };

        return (lastTeleport == 0 ? cooldown : gameTime) - lastTeleport >= cooldown;
    }

    public SkyMeta load(@Nonnull CompoundTag nbt) {
        this.owner = nbt.getUUID(OWNER_ID);
        this.teamId = nbt.getUUID(TEAM_ID);

        this.previousTeamIds.clear();
        for (Tag tag : nbt.getList(PREVIOUS_TEAM_IDS, Tag.TAG_INT_ARRAY)) {
            this.previousTeamIds.add(NbtUtils.loadUUID(tag));
        }

        this.invites.clear();
        for (Tag tag : nbt.getList(INVITATIONS, Tag.TAG_INT_ARRAY)) {
            this.invites.add(NbtUtils.loadUUID(tag));
        }

        this.lastHomeTeleport = nbt.getLong(LAST_HOME_TELEPORT);
        this.lastSpawnTeleport = nbt.getLong(LAST_SPAWN_TELEPORT);
        this.lastVisitTeleport = nbt.getLong(LAST_VISIT_TELEPORT);

        return this;
    }

    @Nonnull
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID(OWNER_ID, this.owner);
        nbt.putUUID(TEAM_ID, this.teamId);

        ListTag prevTeamIds = new ListTag();
        for (UUID id : this.previousTeamIds) {
            prevTeamIds.add(NbtUtils.createUUID(id));
        }

        ListTag invitationTeams = new ListTag();
        for (UUID id : this.invites) {
            invitationTeams.add(NbtUtils.createUUID(id));
        }

        nbt.put(PREVIOUS_TEAM_IDS, prevTeamIds);
        nbt.put(INVITATIONS, invitationTeams);
        nbt.putLong(LAST_HOME_TELEPORT, this.lastHomeTeleport);
        nbt.putLong(LAST_SPAWN_TELEPORT, this.lastSpawnTeleport);
        nbt.putLong(LAST_VISIT_TELEPORT, this.lastVisitTeleport);
        return nbt;
    }

    public enum TeleportType {
        SPAWN,
        HOME,
        VISIT
    }
}
