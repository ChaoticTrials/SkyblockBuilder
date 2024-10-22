package de.melanx.skyblockbuilder;

import de.melanx.skyblockbuilder.config.common.SpawnConfig;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
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
        APPLY_BONEMEAL,
        MOBS_SPAWN,
        MOBS_SPAWN_EGG,
        DAMAGE,
        HEALING
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        Item mainHandItem = event.getEntity().getMainHandItem().getItem();
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(mainHandItem);
        if (SpawnConfig.interactionItemsInSpawnProtection.test(itemKey)) {
            return;
        }

        if (SpawnProtectionEvents.isOnSpawn(event.getEntity()) && !event.getEntity().hasPermissions(2)) {
            if (event instanceof PlayerInteractEvent.EntityInteract entityInteract &&
                    (SpawnConfig.interactionEntitiesInSpawnProtection.test(ForgeRegistries.ENTITY_TYPES.getKey(entityInteract.getTarget().getType()))
                            || SpawnProtectionEvents.ignore(Type.INTERACT_ENTITIES))) {
                return;
            }

            if (event.isCancelable() && !SpawnProtectionEvents.ignore(Type.INTERACT_BLOCKS)) {
                Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
                ResourceLocation blockRegistryKey = ForgeRegistries.BLOCKS.getKey(block);
                boolean allowBlockInteraction = SpawnConfig.interactionBlocksInSpawnProtection.test(blockRegistryKey);

                event.setCanceled(!allowBlockInteraction);
            }
        }
    }

    @SubscribeEvent
    public void mobGrief(EntityMobGriefingEvent event) {
        if (SpawnProtectionEvents.ignore(Type.MOB_GRIEFING)) {
            return;
        }

        //noinspection ConstantConditions
        if (event.getEntity() != null && event.getEntity().level() != null && event.getEntity().level().dimension() != null) {
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

        Vec3 position = event.getExplosion().getPosition();
        if (SpawnProtectionEvents.isOnSpawn(event.getLevel(), new BlockPos((int) position.x, (int) position.y, (int) position.z))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent event) {
        if (SpawnProtectionEvents.ignore(Type.INTERACT_BLOCKS)) {
            return;
        }

        if (SpawnProtectionEvents.isOnSpawn(event.getPlayer()) && !event.getPlayer().hasPermissions(2)) {
            Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
            ResourceLocation blockRegistryKey = ForgeRegistries.BLOCKS.getKey(block);
            boolean allowBlockInteraction = SpawnConfig.interactionBlocksInSpawnProtection.test(blockRegistryKey);

            event.setCanceled(!allowBlockInteraction);
        }
    }

    @SubscribeEvent
    public void blockPlace(BlockEvent.EntityPlaceEvent event) {
        if (SpawnProtectionEvents.ignore(Type.INTERACT_BLOCKS)) {
            return;
        }

        if (event.getLevel() instanceof Level level && SpawnProtectionEvents.isOnSpawn(level, event.getPos())) {
            if (!(event.getEntity() instanceof Player) || !event.getEntity().hasPermissions(2)) {
                Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
                ResourceLocation blockRegistryKey = ForgeRegistries.BLOCKS.getKey(block);
                boolean allowBlockInteraction = SpawnConfig.interactionBlocksInSpawnProtection.test(blockRegistryKey);

                event.setCanceled(!allowBlockInteraction);
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
    public void applyBonemeal(BonemealEvent event) {
        if (SpawnProtectionEvents.ignore(Type.APPLY_BONEMEAL)) {
            return;
        }

        if (SpawnProtectionEvents.isOnSpawn(event.getLevel(), event.getPos()) && (event.getEntity() == null || !event.getEntity().hasPermissions(2))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void mobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (SpawnProtectionEvents.ignore(Type.MOBS_SPAWN_EGG)) {
            return;
        }

        Level level;
        if (event.getLevel() instanceof Level) level = (Level) event.getLevel();
        else level = event.getEntity().level();
        if (level != null && SpawnProtectionEvents.isOnSpawn(event.getEntity())) {
            if (event.getSpawnType() != MobSpawnType.SPAWN_EGG && event.getSpawnType() != MobSpawnType.BUCKET
                    && event.getSpawnType() != MobSpawnType.MOB_SUMMONED && event.getSpawnType() != MobSpawnType.COMMAND) {
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

        if (!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && SpawnProtectionEvents.isOnSpawn(event.getEntity()) && (!(event.getSource().getEntity() instanceof Player) || !event.getSource().getEntity().hasPermissions(2))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void livingHurt(LivingHurtEvent event) {
        if (SpawnProtectionEvents.ignore(Type.DAMAGE)) {
            return;
        }

        if (!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && SpawnProtectionEvents.isOnSpawn(event.getEntity()) && (event.getEntity() instanceof Player || !(event.getSource().getEntity() instanceof Player) || !event.getSource().getEntity().hasPermissions(2))) {
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

        if (!event.player.level().isClientSide && !event.player.isDeadOrDying() && event.player.tickCount % 20 == 0 && SpawnProtectionEvents.isOnSpawn(event.player)) {
            event.player.setHealth(event.player.getMaxHealth());
            event.player.getFoodData().setFoodLevel(20);
            event.player.setAirSupply(event.player.getMaxAirSupply());
            event.player.setRemainingFireTicks(0);
        }
    }

    private static boolean ignore(Type type) {
        return !SpawnConfig.spawnProtectionEvents.contains(type);
    }

    private static boolean isOnSpawn(Entity entity) {
        return SpawnProtectionEvents.isOnSpawn(entity.level(), entity.blockPosition());
    }

    private static boolean isOnSpawn(Level level, BlockPos blockPos) {
        ChunkPos pos = new ChunkPos(blockPos);
        return WorldUtil.isSkyblock(level) && SpawnConfig.dimension == level.dimension()
                && Math.abs(pos.x) < SpawnConfig.spawnProtectionRadius && Math.abs(pos.z) < SpawnConfig.spawnProtectionRadius
                && !level.isOutsideBuildHeight(blockPos);
    }
}
