package de.melanx.skyblockbuilder.events;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

/**
 * This event fires when something is about to send a player to another dimension.<br>
 * <br>
 * With a dimension per team, {@link Level#OVERWORLD} and {@link Level#NETHER} may not be the same for everyone, every
 * team may have its own copy of them. Mods teleporting players on their own have no way to know that, so they should fire
 * this event and use {@link #getDimension()} instead of the dimension they started with.<br>
 * <br>
 * Skyblock Builder fires this for a few mods itself, see the {@code coremods} module.<br>
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}
 */
public class SkyblockChangeDimensionEvent extends Event {

    private final ServerPlayer player;
    private final ResourceKey<Level> originalDimension;
    private ResourceKey<Level> dimension;

    public SkyblockChangeDimensionEvent(ServerPlayer player, ResourceKey<Level> dimension) {
        this.player = player;
        this.originalDimension = dimension;
        this.dimension = dimension;
    }

    /**
     * @return Teleporting player
     */
    public ServerPlayer getPlayer() {
        return this.player;
    }

    /**
     * @return The dimension the player was asked to be sent to
     */
    public ResourceKey<Level> getOriginalDimension() {
        return this.originalDimension;
    }

    /**
     * @return The dimension the player should be sent to
     */
    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }
}
