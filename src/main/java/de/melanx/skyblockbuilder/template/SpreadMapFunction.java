package de.melanx.skyblockbuilder.template;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.registration.ModLootItemFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpreadMapFunction extends LootItemConditionalFunction {

    public static final Holder<MapDecorationType> DEFAULT_DECORATION = MapDecorationTypes.RED_X;
    public static final MapCodec<SpreadMapFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> LootItemConditionalFunction.commonFields(instance)
                    .and(
                            instance.group(
                                    Codec.either(
                                            Codec.STRING.listOf(),
                                            Codec.STRING
                                    ).xmap(
                                            either -> either.map(Function.identity(), List::of),
                                            list -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list)
                                    ).fieldOf("spreads").forGetter(function -> function.spreadNames),
                                    MapDecorationType.CODEC.optionalFieldOf("decoration", DEFAULT_DECORATION).forGetter(function -> function.mapDecoration),
                                    Codec.BYTE.optionalFieldOf("zoom", (byte) 2).forGetter(function -> function.zoom)
                            )
                    )
                    .apply(instance, SpreadMapFunction::new)
    );
    private final List<String> spreadNames;
    private final Holder<MapDecorationType> mapDecoration;
    private final byte zoom;

    public SpreadMapFunction(List<LootItemCondition> conditions, List<String> spreadNames, Holder<MapDecorationType> mapDecoration, byte zoom) {
        super(conditions);
        this.spreadNames = spreadNames;
        this.mapDecoration = mapDecoration;
        this.zoom = zoom;
    }

    @Nonnull
    @Override
    protected ItemStack run(@Nonnull ItemStack stack, @Nonnull LootContext context) {
        if (!stack.is(Items.MAP)) {
            return stack;
        }

        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        Vec3 pos = context.getParamOrNull(LootContextParams.ORIGIN);
        if (!(entity instanceof ServerPlayer player) || pos == null) {
            return stack;
        }

        ServerLevel level = (ServerLevel) player.level();
        SkyblockSavedData data = SkyblockSavedData.get(level);
        Team team = data.getTeamFromPlayer(player);
        if (team == null) {
            SkyblockBuilder.getLogger().error("Player {} does not have a team.", player);
            return stack;
        }

        if (team.getPlacedSpreads().isEmpty()) {
            SkyblockBuilder.getLogger().error("Team {} has no placed spreads", team.getName());
            return stack;
        }

        Set<Team.PlacedSpread> placedSpreads = new HashSet<>();
        for (String spreadName : this.spreadNames) {
            placedSpreads.addAll(team.getPlacedSpreads(spreadName));
        }

        if (placedSpreads.isEmpty()) {
            SkyblockBuilder.getLogger().error("No spread {} for team {}", this.spreadNames, team.getName());
            return stack;
        }

        BlockPos middle = SpreadMapFunction.getMiddle(placedSpreads.stream().map(Team.PlacedSpread::pos).collect(Collectors.toSet()));
        middle = middle.offset(0, 0, 0);
        ItemStack map = SpreadMapFunction.createFixedMap(level, middle.getX(), middle.getZ(), this.zoom, true, true);
        MapItem.renderBiomePreviewMap(level, map);

        int i = 0;
        for (Team.PlacedSpread placedSpread : placedSpreads) {
            MapItemSavedData.addTargetDecoration(map, placedSpread.pos(), String.valueOf(i++), this.mapDecoration);
        }

        return map;
    }

    public static BlockPos getMiddle(Set<BlockPos> blocks) {
        double x = 0, z = 0;
        for (BlockPos pos : blocks) {
            x += pos.getX();
            z += pos.getZ();
        }

        int avgX = (int) (x / blocks.size());
        int avgZ = (int) (z / blocks.size());

        return new BlockPos(avgX, 0, avgZ);
    }

    // We need to create our own MapItemSavedData since MapItem#create is off-centered when scale is 1 or higher
    // Sadly, the map will not stay fixed when resizing
    // Issue here: https://bugs.mojang.com/browse/MC-142694
    private static ItemStack createFixedMap(Level level, int levelX, int levelZ, byte scale, boolean trackingPosition, boolean unlimitedTracking) {
        ItemStack map = Items.FILLED_MAP.getDefaultInstance();
        MapItemSavedData data = new MapItemSavedData(levelX, levelZ, scale, trackingPosition, unlimitedTracking, false, level.dimension());
        MapId freeMapId = level.getFreeMapId();
        level.setMapData(freeMapId, data);
        map.set(DataComponents.MAP_ID, freeMapId);

        return map;
    }

    @Nonnull
    @Override
    public LootItemFunctionType<SpreadMapFunction> getType() {
        return ModLootItemFunctions.spreadMap;
    }
}
