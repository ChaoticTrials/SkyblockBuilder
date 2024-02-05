package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class SpawnProtectionEvents {

    public enum Type {
        INTERACT_ENTITIES,
        INTERACT_BLOCKS,
        MOB_GRIEFING,
        EXPLOSIONS,
        CROP_GROW,
        MOBS_SPAWN,
        MOBS_SPAWN_EGG,
        DAMAGE,
        HEALING
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (SpawnProtectionEvents.isOnSpawn(event.getEntity()) && !event.getEntity().hasPermissions(2)) {
            if (event instanceof PlayerInteractEvent.EntityInteract entityInteract &&
                    (ConfigHandler.Spawn.interactionEntitiesInSpawnProtection.test(ForgeRegistries.ENTITY_TYPES.getKey(entityInteract.getTarget().getType()))
                            || SpawnProtectionEvents.ignore(Type.INTERACT_ENTITIES))) {
                return;
            }

            if (event.isCancelable() && !SpawnProtectionEvents.ignore(Type.INTERACT_BLOCKS)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void mobGrief(EntityMobGriefingEvent event) {
        if (SpawnProtectionEvents.ignore(Type.MOB_GRIEFING)) {
            return;
        }

        //noinspection ConstantConditions
        if (event.getEntity() != null && event.getEntity().level != null && event.getEntity().level.dimension() != null) {
            if (SpawnProtectionEvents.isOnSpawn(event.getEntity())) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public void explode(ExplosionEvent.Start event) {
        if (SpawnProtectionEvents.ignore(Type.EXPLOSIONS)) {
            return;
        }

        if (SpawnProtectionEvents.isOnSpawn(event.getLevel(), new BlockPos(event.getExplosion().getPosition()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent event) {
        if (SpawnProtectionEvents.ignore(Type.INTERACT_BLOCKS)) {
            return;
        }

        if (SpawnProtectionEvents.isOnSpawn(event.getPlayer()) && !event.getPlayer().hasPermissions(2)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent event) {
        if (SpawnProtectionEvents.ignore(Type.INTERACT_BLOCKS)) {
            return;
        }

        if (event.getLevel() instanceof Level level && SpawnProtectionEvents.isOnSpawn(level, event.getPos())) {
            if (!(event.getEntity() instanceof Player) || !event.getEntity().hasPermissions(2)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void blockMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        if (SpawnProtectionEvents.ignore(Type.INTERACT_BLOCKS)) {
            return;
        }

        if (event.getLevel() instanceof Level level && SpawnProtectionEvents.isOnSpawn(level, event.getPos())) {
            if (!(event.getEntity() instanceof Player) || !event.getEntity().hasPermissions(2)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void farmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (SpawnProtectionEvents.ignore(Type.MOB_GRIEFING)) {
            return;
        }

        if (SpawnProtectionEvents.isOnSpawn(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void cropGrow(BlockEvent.CropGrowEvent.Pre event) {
        if (SpawnProtectionEvents.ignore(Type.CROP_GROW)) {
            return;
        }

        if (event.getLevel() instanceof Level level && SpawnProtectionEvents.isOnSpawn(level, event.getPos())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void cropGrow(BlockEvent.BlockToolModificationEvent event) {
        if (SpawnProtectionEvents.ignore(Type.CROP_GROW)) {
            return;
        }

        if (SpawnProtectionEvents.isOnSpawn(event.getContext().getLevel(), event.getPos()) && (event.getPlayer() == null || !event.getPlayer().hasPermissions(2))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void mobSpawnAttempt(LivingSpawnEvent.CheckSpawn event) {
        if (SpawnProtectionEvents.ignore(Type.MOBS_SPAWN)) {
            return;
        }

        Level level;
        if (event.getLevel() instanceof Level) level = (Level) event.getLevel();
        else level = event.getEntity().level;
        if (level != null && SpawnProtectionEvents.isOnSpawn(event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void mobSpawn(LivingSpawnEvent.SpecialSpawn event) {
        if (SpawnProtectionEvents.ignore(Type.MOBS_SPAWN_EGG)) {
            return;
        }

        Level level;
        if (event.getLevel() instanceof Level) level = (Level) event.getLevel();
        else level = event.getEntity().level;
        if (level != null && SpawnProtectionEvents.isOnSpawn(event.getEntity())) {
            if (event.getSpawnReason() != MobSpawnType.SPAWN_EGG && event.getSpawnReason() != MobSpawnType.BUCKET
                    && event.getSpawnReason() != MobSpawnType.MOB_SUMMONED && event.getSpawnReason() != MobSpawnType.COMMAND) {
                if (event.isCancelable()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void livingAttack(LivingAttackEvent event) {
        if (SpawnProtectionEvents.ignore(Type.DAMAGE)) {
            return;
        }

        if (!event.getSource().isBypassInvul() && SpawnProtectionEvents.isOnSpawn(event.getEntity()) && (!(event.getSource().getEntity() instanceof Player) || !event.getSource().getEntity().hasPermissions(2))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void livingHurt(LivingHurtEvent event) {
        if (SpawnProtectionEvents.ignore(Type.DAMAGE)) {
            return;
        }

        if (!event.getSource().isBypassInvul() && SpawnProtectionEvents.isOnSpawn(event.getEntity()) && (event.getEntity() instanceof Player || !(event.getSource().getEntity() instanceof Player) || !event.getSource().getEntity().hasPermissions(2))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void attackEntity(AttackEntityEvent event) {
        if (SpawnProtectionEvents.ignore(Type.DAMAGE) || SpawnProtectionEvents.ignore(Type.INTERACT_ENTITIES)) {
            return;
        }

        if (SpawnProtectionEvents.isOnSpawn(event.getTarget()) && !event.getEntity().hasPermissions(2)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (SpawnProtectionEvents.ignore(Type.HEALING)) {
            return;
        }

        if (!event.player.level.isClientSide && !event.player.isDeadOrDying() && event.player.tickCount % 20 == 0 && SpawnProtectionEvents.isOnSpawn(event.player)) {
            event.player.setHealth(event.player.getMaxHealth());
            event.player.getFoodData().setFoodLevel(20);
            event.player.setAirSupply(event.player.getMaxAirSupply());
            event.player.setRemainingFireTicks(0);
        }
    }

    private static boolean ignore(Type type) {
        return !ConfigHandler.Spawn.spawnProtectionEvents.contains(type);
    }

    private static boolean isOnSpawn(Level level, BlockPos blockPos) {
        ChunkPos pos = new ChunkPos(blockPos);
        return WorldUtil.isSkyblock(level) && ConfigHandler.Spawn.dimension == level.dimension()
                && Math.abs(pos.x) < ConfigHandler.Spawn.spawnProtectionRadius && Math.abs(pos.z) < ConfigHandler.Spawn.spawnProtectionRadius;
    }

    private static boolean isOnSpawn(Entity entity) {
        ChunkPos pos = new ChunkPos(entity.blockPosition());
        return WorldUtil.isSkyblock(entity.level) && ConfigHandler.Spawn.dimension == entity.level.dimension()
                && Math.abs(pos.x) < ConfigHandler.Spawn.spawnProtectionRadius && Math.abs(pos.z) < ConfigHandler.Spawn.spawnProtectionRadius;
    }
}
