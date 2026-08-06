package de.melanx.skyblockbuilder.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import net.minecraft.core.UUIDUtil;

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

    public static final Codec<SkyMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf(OWNER_ID).forGetter(meta -> meta.owner),
            UUIDUtil.CODEC.optionalFieldOf(TEAM_ID, SkyblockSavedData.SPAWN_ID).forGetter(meta -> meta.teamId),
            UUIDUtil.CODEC.listOf().optionalFieldOf(PREVIOUS_TEAM_IDS, List.of()).forGetter(meta -> List.copyOf(meta.previousTeamIds)),
            UUIDUtil.CODEC.listOf().optionalFieldOf(INVITATIONS, List.of()).forGetter(meta -> List.copyOf(meta.invites)),
            Codec.LONG.optionalFieldOf(LAST_HOME_TELEPORT, 0L).forGetter(meta -> meta.lastHomeTeleport),
            Codec.LONG.optionalFieldOf(LAST_SPAWN_TELEPORT, 0L).forGetter(meta -> meta.lastSpawnTeleport),
            Codec.LONG.optionalFieldOf(LAST_VISIT_TELEPORT, 0L).forGetter(meta -> meta.lastVisitTeleport)
    ).apply(instance, SkyMeta::new));

    private final Set<UUID> previousTeamIds = Sets.newHashSet();
    private final List<UUID> invites = Lists.newArrayList();
    private final UUID owner;
    private SkyblockSavedData data;
    private UUID teamId = SkyblockSavedData.SPAWN_ID;
    private long lastHomeTeleport;
    private long lastSpawnTeleport;
    private long lastVisitTeleport;

    public SkyMeta(SkyblockSavedData data, UUID owner) {
        this.data = data;
        this.owner = owner;
    }

    private SkyMeta(UUID owner, UUID teamId, List<UUID> previousTeamIds, List<UUID> invites,
            long lastHomeTeleport, long lastSpawnTeleport, long lastVisitTeleport) {
        // decoded metas are unbound; SkyblockSavedData calls bindData once it exists
        this.data = null;
        this.owner = owner;
        this.teamId = teamId;
        this.previousTeamIds.addAll(previousTeamIds);
        this.invites.addAll(invites);
        this.lastHomeTeleport = lastHomeTeleport;
        this.lastSpawnTeleport = lastSpawnTeleport;
        this.lastVisitTeleport = lastVisitTeleport;
    }

    void bindData(SkyblockSavedData data) {
        this.data = data;
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

    public enum TeleportType {
        SPAWN,
        HOME,
        VISIT
    }
}
