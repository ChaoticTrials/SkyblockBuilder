package de.melanx.skyblockbuilder.events;

import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event fires when a player tries to teleport home.<br>
 * <br>
 * This event is {@link net.minecraftforge.eventbus.api.Cancelable}.<br>
 * <br>
 * This event does have a result. {@link net.minecraftforge.eventbus.api.Event.HasResult}<br>
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}
 */
public class SkyblockTeleportHomeEvent extends Event {

    private final ServerPlayerEntity player;
    private final Team team;

    public SkyblockTeleportHomeEvent(ServerPlayerEntity player, Team team) {
        this.player = player;
        this.team = team;
    }

    /**
     * @return Teleporting player
     */
    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    /**
     * @return Team from the player
     */
    public Team getTeam() {
        return this.team;
    }

    @Override
    public boolean hasResult() {
        return true;
    }
}
